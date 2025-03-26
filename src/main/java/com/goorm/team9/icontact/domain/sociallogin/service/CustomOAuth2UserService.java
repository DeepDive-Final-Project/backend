package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.enums.Status;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.client.service.ClientSaveService;
import com.goorm.team9.icontact.domain.sociallogin.entity.LoginHistory;
import com.goorm.team9.icontact.domain.sociallogin.entity.OAuth;
import com.goorm.team9.icontact.domain.sociallogin.repository.LoginHistoryRepository;
import com.goorm.team9.icontact.domain.sociallogin.repository.OAuthRepository;
import com.goorm.team9.icontact.domain.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProviderFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * OAuth 로그인 시 사용자 정보를 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthProviderFactory providerFactory;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientRepository clientRepository;
    private final OAuthRepository oAuthRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ClientSaveService clientSaveService;

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        logger.info("📌 리디렉션 처리 중 - URI: /login/oauth2/code/{} 요청 도착", userRequest.getClientRegistration().getRegistrationId());

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                provider, userRequest.getClientRegistration().getClientId());

        String refreshToken = authorizedClient != null && authorizedClient.getRefreshToken() != null
                ? authorizedClient.getRefreshToken().getTokenValue()
                : null;
        logger.info("🛠 Access Token: {}", accessToken);

        OAuthProvider oAuthProvider = providerFactory.getProvider(provider);
        if (oAuthProvider == null) {
            throw new RuntimeException("지원하지 않는 OAuth 제공자: " + provider);
        }

        Map<String, Object> userInfo = oAuthProvider.getUserInfo(accessToken);
        long expiresAt = oAuthProvider.getTokenExpiry(accessToken);

        String oauthUserId = String.valueOf(userInfo.getOrDefault("id", userInfo.get("sub")));
        if (oauthUserId == null || oauthUserId.isEmpty()) {
            throw new RuntimeException("❌ OAuth2User ID가 누락되었습니다.");
        }

        String email = (String) userInfo.get("email");
        if ("github".equals(provider) && (email == null || email.isEmpty())) {
            email = getPrimaryEmailFromGitHub(accessToken);
            if (email == null) {
                throw new RuntimeException("❌ GitHub 이메일 정보를 가져올 수 없습니다.");
            }
        }

        final String userEmail = email;
        final String normalizedProvider = provider.toLowerCase();

        logger.info("🔍 사용자 조회 - email={}, provider={}", userEmail, normalizedProvider);

        ClientEntity client = clientRepository.findByEmailAndProviderAndIsDeletedFalse(userEmail, normalizedProvider)
                .orElse(null);

        if (client == null) {
            Optional<ClientEntity> deletedClient = clientRepository.findByEmailAndProviderAndIsDeletedTrue(userEmail, normalizedProvider);
            if (deletedClient.isPresent()) {
                logger.warn("🚫 탈퇴한 사용자 - 로그인 차단: email={}, provider={}", userEmail, normalizedProvider);
                throw new RuntimeException("탈퇴한 사용자입니다. 복구 후 이용해주세요.");
            }

            // 3. 신규 사용자 저장
            ClientEntity clientEntityToSave = ClientEntity.builder()
                    .nickName(NicknameGeneratorService.generateNickname())
                    .email(userEmail)
                    .provider(normalizedProvider)
                    .role(Role.DEV)
                    .status(Status.PUBLIC)
                    .isDeleted(false)
                    .build();

            logger.info("📝 사용자 저장 시도 - email={}, provider={}", userEmail, normalizedProvider);
            client = clientSaveService.saveClientSafely(clientEntityToSave);
            logger.info("✅ 사용자 저장 완료 - id={}", client.getId());
        } else {
            logger.info("✅ 기존 사용자 조회 성공 - id={}", client.getId());
        }
        if (client.getProvider() == null || !client.getProvider().equalsIgnoreCase(provider)) {
            client.setProvider(normalizedProvider);
            clientRepository.save(client);
        }

        OAuth oauth = oAuthRepository.findByProviderAndEmail(normalizedProvider, userEmail).orElse(null);
        if (oauth == null) {
            logger.info("🆕 새로운 OAuth 계정 저장 - provider={}, email={}", normalizedProvider, userEmail);
            oauth = OAuth.builder()
                    .provider(normalizedProvider)
                    .email(userEmail)
                    .client(client)
                    .oauthUserId(oauthUserId)
                    .accessToken(accessToken)
                    .refreshToken(null)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        oauth.updateAccessToken(accessToken);
        if (refreshToken != null) {
            oauth.updateRefreshToken(refreshToken);
        }
        oAuthRepository.save(oauth);

        loginHistoryRepository.save(LoginHistory.builder()
                .clientEntity(client)
                .provider(normalizedProvider)
                .loginAt(LocalDateTime.now())
                .build());

        String jwtToken = jwtTokenProvider.createToken(email, expiresAt, provider);
        userInfo.put("jwtToken", jwtToken);
        userInfo.put("email", email);

        logger.info("✅ {} 로그인 성공 - JWT 발급 완료: {}", normalizedProvider, jwtToken);
        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), userInfo, "email");
    }

    public String refreshAccessToken(String provider, String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("리프레시 토큰이 없습니다. 다시 로그인하세요.");
        }

        OAuth oauth = oAuthRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 리프레시 토큰입니다. 다시 로그인하세요."));

        String newAccessToken = switch (provider) {
            case "google" -> refreshGoogleAccessToken(refreshToken);
            case "kakao" -> refreshKakaoAccessToken(refreshToken);
            case "github" -> refreshGitHubAccessToken(refreshToken);
            default -> throw new RuntimeException("지원하지 않는 OAuth 제공자: " + provider);
        };

        if (newAccessToken == null) {
            throw new RuntimeException("새로운 액세스 토큰을 가져오지 못했습니다. 다시 로그인하세요.");
        }

        oauth.updateAccessToken(newAccessToken);
        oAuthRepository.save(oauth);

        return newAccessToken;
    }

    private String refreshGoogleAccessToken(String refreshToken) {
        String url = "https://oauth2.googleapis.com/token";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String body = "client_id=" + System.getenv("GOOGLE_CLIENT_ID") +
                "&client_secret=" + System.getenv("GOOGLE_CLIENT_SECRET") +
                "&refresh_token=" + refreshToken +
                "&grant_type=refresh_token";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        return (response.getBody() != null) ? (String) response.getBody().get("access_token") : null;
    }

    private String refreshKakaoAccessToken(String refreshToken) {
        String url = "https://kauth.kakao.com/oauth/token";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String body = "grant_type=refresh_token" +
                "&client_id=" + System.getenv("KAKAO_CLIENT_ID") +
                "&client_secret=" + System.getenv("KAKAO_CLIENT_SECRET") +
                "&refresh_token=" + refreshToken;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        return (response.getBody() != null) ? (String) response.getBody().get("access_token") : null;
    }

    private String refreshGitHubAccessToken(String refreshToken) {
        throw new RuntimeException("GitHub OAuth는 리프레시 토큰을 지원하지 않습니다. 다시 로그인하세요.");
    }

    /**
     * GitHub API를 사용하여 사용자의 기본 이메일을 가져오는 메서드
     */
    private String getPrimaryEmailFromGitHub(String accessToken) {
        String url = "https://api.github.com/user/emails";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, (Class<List<Map<String, Object>>>) (Object) List.class
        );

        if (response.getBody() != null) {
            for (Map<String, Object> emailData : response.getBody()) {
                Boolean primary = (Boolean) emailData.get("primary");
                Boolean verified = (Boolean) emailData.get("verified");
                String email = (String) emailData.get("email");

                if (primary != null && primary && verified != null && verified) {
                    return email; // 기본 이메일 반환
                }
            }
        }

        return null; // 이메일 정보를 가져오지 못하면 null 반환
    }

}