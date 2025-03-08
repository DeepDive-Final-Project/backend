package com.goorm.team9.icontact.sociallogin.service;

import com.goorm.team9.icontact.sociallogin.domain.OAuth;
import com.goorm.team9.icontact.sociallogin.domain.User;
import com.goorm.team9.icontact.sociallogin.repository.OAuthRepository;
import com.goorm.team9.icontact.sociallogin.repository.UserRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;
    private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);

    @Transactional
    public User saveOrUpdateUser(String provider, String oauthUserId, String email, String nickname,
                                 String accessToken, String refreshToken, LocalDateTime expiresAt) {

        // GitHub OAuth의 경우 이메일이 비어있을 수 있으므로 이메일 가져오기
        if ("github".equalsIgnoreCase(provider) && (email == null || email.isEmpty())) {
            email = fetchGitHubEmail(accessToken);
            if (email == null) {
                throw new RuntimeException("GitHub 이메일을 가져올 수 없습니다.");
            }
        }

        // 기존 OAuth 정보 조회
        Optional<OAuth> existingOAuth = oauthRepository.findByProviderAndOauthUserId(provider, oauthUserId);
        if (existingOAuth.isPresent()) {
            return existingOAuth.get().getUser();
        }

        final String finalEmail = email;

        // 기존 이메일을 가진 User가 존재하는지 확인
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .nickname(nickname)
                                .email(finalEmail)
                                .age(25L)  // 기본값 설정
                                .industry(null)
                                .role(null)
                                .career(null)
                                .status(null)
                                .introduction("")
                                .link("")
                                .profileImage(null)
                                .chatOpportunity(0L)
                                .chatMessage(0L)
                                .offline(false)
                                .isDeleted(false)
                                .deletedAt(null)
                                .build()
                ));

        // OAuth 정보 저장
        OAuth oauth = OAuth.builder()
                .provider(provider)
                .oauthUserId(oauthUserId)
                .email(email)
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        oauthRepository.save(oauth);
        logger.info("✅ OAuth 계정 저장 완료: {}", email);

        return user;
    }

    @Transactional
    public void invalidateAccessToken(String email) {
        oauthRepository.findByEmail(email).ifPresent(oauth -> {
            logger.info("🔴 로그아웃 처리 - accessToken 제거 전: {}", oauth.getAccessToken());
            oauth.updateAccessToken(null);
            oauthRepository.save(oauth);
            oauthRepository.flush(); // 강제 DB 반영
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

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/user/emails", HttpMethod.GET, entity,
                new ParameterizedTypeReference<>() {});

        return response.getBody().stream()
                .filter(email -> (boolean) email.get("primary"))
                .map(email -> (String) email.get("email"))
                .findFirst()
                .orElse(null);
    }
}