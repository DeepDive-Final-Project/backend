package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthTokenResponseDto;
import com.goorm.team9.icontact.domain.sociallogin.entity.OAuth;
import com.goorm.team9.icontact.domain.sociallogin.repository.OAuthRepository;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProviderFactory;
import com.nimbusds.jwt.JWT;
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

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final ClientRepository clientRepository;
    private final OAuthRepository oauthRepository;
    private final OAuthProviderFactory providerFactory;
    private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);

    public OAuthTokenResponseDto authenticateWithOAuth(String provider, String code) {
        OAuthProvider oAuthProvider = providerFactory.getProvider(provider);
        String accessToken = oAuthProvider.getAccessToken(code);
        long expiresAt = oAuthProvider.getTokenExpiry(accessToken);
        Map<String, Object> userInfo = oAuthProvider.getUserInfo(accessToken);

        String email = (String) userInfo.get("email");
        saveOrUpdateUser(provider, email, accessToken);

        return new OAuthTokenResponseDto(email, expiresAt);
    }

    public String getAccessToken(String provider, String code) {
        OAuthProvider oAuthProvider = providerFactory.getProvider(provider);
        return oAuthProvider.getAccessToken(code);
    }

    @Transactional
    public void saveOrUpdateUser(String provider, String email, String accessToken) {

        ClientEntity client = clientRepository.findByEmailAndProviderNative(email, provider)
                .orElseGet(() -> {
                    logger.info("🆕 새로운 ClientEntity 생성: email={}, provider={}", email, provider);
                    ClientEntity newClient = ClientEntity.builder()
                            .email(email)
                            .provider(provider)
                            .nickName(NicknameGeneratorService.generateNickname())
                            .role(Role.DEV)
                            .isDeleted(false)
                            .build();
                    return clientRepository.save(newClient);
                });

        OAuth oauth = oauthRepository.findByProviderAndEmail(provider, email)
                .orElseGet(() -> {
                    logger.info("🆕 새로운 OAuth 계정 저장: provider={}, email={}", provider, email);
                    return OAuth.builder()
                            .provider(provider)
                            .email(email)
                            .client(client)
                            .accessToken(accessToken)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .expiresAt(LocalDateTime.now().plusDays(30))
                            .build();
                });

        oauth.updateAccessToken(accessToken);
        oauthRepository.save(oauth);
        logger.info("✅ OAuth 저장 완료: email={}, provider={}", email, provider);
    }

    @Transactional
    public void invalidateAccessToken(String email) {
        oauthRepository.findByEmail(email).ifPresent(oauth -> {
            logger.info("🔴 로그아웃 처리 - accessToken 제거 전: {}", oauth.getAccessToken());
            oauth.updateAccessToken(null);
            oauthRepository.save(oauth);
            logger.info("✅ 로그아웃 완료 - DB에서 accessToken 제거됨: {}", email);
        });
    }

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