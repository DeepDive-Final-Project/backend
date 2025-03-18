package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.enums.Status;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthTokenResponse;
import com.goorm.team9.icontact.domain.sociallogin.entity.OAuth;
import com.goorm.team9.icontact.domain.sociallogin.repository.OAuthRepository;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProviderFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth 인증 및 사용자 정보 관리 서비스.
 * - OAuth 계정 저장 및 갱신
 * - GitHub 로그인 처리
 * - Access Token 무효화
 */
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final ClientRepository clientRepository;
    private final OAuthRepository oauthRepository;
    private final OAuthProviderFactory providerFactory;
    private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);

    /**
     * OAuth 로그인 처리
     * - 사용자 정보를 가져오고 JWT 발급을 담당
     *
     * @param code  발급한 인증 코드
     * @return JWT 토큰 (이후 AuthService에서 사용)
     */
    public OAuthTokenResponse authenticateWithGithub(String provider, String code) {
        OAuthProvider oAuthProvider = providerFactory.getProvider(provider);
        if (oAuthProvider == null) {
            throw new IllegalArgumentException("지원하지 않는 OAuth 제공자: " + provider);
        }

        String accessToken = oAuthProvider.getAccessToken(code);
        long expiresAt = oAuthProvider.getTokenExpiry(accessToken);
        Map<String, Object> userInfo = oAuthProvider.getUserInfo(accessToken);

        String oauthUserId = userInfo.get("id").toString();
        String email = (String) userInfo.getOrDefault("email", "no-email");
        String nickname = (String) userInfo.getOrDefault("login", "unknown");

        if ("no-email".equals(email) || email == null) {
            email = fetchGitHubEmail(accessToken);
        }

        logger.info("✅ {} 로그인 완료: {}", provider, email);
        return new OAuthTokenResponse(email, expiresAt); // OAuthTokenResponse 객체 반환
    }

    /**
     * Access Token을 가져오는 메서드
     */
    public String getAccessToken(String provider, String code) {
        OAuthProvider oAuthProvider = providerFactory.getProvider(provider);
        return oAuthProvider.getAccessToken(code);
    }

    /**
     * OAuth 계정을 저장하거나 기존 정보를 업데이트
     */
    @Transactional
    public void saveOrUpdateUser(String provider, String email, String accessToken) {
        // 기존 OAuth 정보 확인
        Optional<OAuth> existingOAuth = oauthRepository.findByProviderAndOauthUserId(provider, email);
        if (existingOAuth.isPresent()) {
            OAuth oauth = existingOAuth.get();
            oauth.updateAccessToken(accessToken); // ✅ 기존 계정이면 accessToken 갱신
            oauthRepository.save(oauth);
            logger.info("🔄 기존 OAuth 계정 accessToken 업데이트: {}", email);
            return;
        }

        ClientEntity clientEntity = clientRepository.findByEmail(email).orElse(null);

        if (clientEntity == null) {
            clientEntity = clientRepository.save(ClientEntity.builder()
                    .nickName(email.split("@")[0]) // 기본 닉네임 설정
                    .email(email)
                    .role(Role.DEV)
                    .status(Status.PUBLIC)
                    .isDeleted(false)
                    .build());
            logger.info("✅ 새로운 Client 저장 완료: {}", clientEntity.getId());
        } else {
            logger.info("🔹 기존 Client 존재: {}", clientEntity.getId());
        }

        OAuth oauth = OAuth.builder()
                .provider(provider)
                .email(email)
                .client(clientEntity)
                .accessToken(accessToken)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        oauthRepository.save(oauth);
        logger.info("✅ OAuth 계정 저장 완료: {}", email);
    }

    /**
     * OAuth 액세스 토큰을 무효화 (로그아웃 시 사용)
     */
    @Transactional
    public void invalidateAccessToken(String email) {
        oauthRepository.findByEmail(email).ifPresent(oauth -> {
            logger.info("🔴 로그아웃 처리 - accessToken 제거 전: {}", oauth.getAccessToken());
            oauth.updateAccessToken(null);
            oauthRepository.save(oauth);
            logger.info("✅ 로그아웃 완료 - DB에서 accessToken 제거됨: {}", email);
        });
    }

    /**
     * GitHub OAuth 로그인 시 이메일 정보를 가져오는 메서드
     */
    private String fetchGitHubEmail(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "https://api.github.com/user/emails", HttpMethod.GET, entity,
                    new ParameterizedTypeReference<>() {});

            return response.getBody().stream()
                    .filter(email -> email.get("primary") != null && (Boolean) email.get("primary"))
                    .map(email -> (String) email.get("email"))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.error("❌ GitHub 이메일 조회 실패: {}", e.getMessage());
            return null;
        }
    }
}
