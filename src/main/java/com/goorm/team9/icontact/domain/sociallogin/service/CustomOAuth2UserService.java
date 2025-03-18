package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProviderFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

/**
 * OAuth 로그인 시 사용자 정보를 처리하는 서비스
 * - OAuth2User를 로드하고 JWT를 생성하여 반환
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthProviderFactory providerFactory;
    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "github", "google", "kakao"
        String accessToken = userRequest.getAccessToken().getTokenValue(); // OAuth Access Token 가져오기
        logger.info("🛠 Access Token: {}", accessToken);

        OAuthProvider oAuthProvider = providerFactory.getProvider(provider);
        if (oAuthProvider == null) {
            throw new RuntimeException("지원하지 않는 OAuth 제공자: " + provider);
        }

        Map<String, Object> userInfo = oAuthProvider.getUserInfo(accessToken);
        long expiresAt = oAuthProvider.getTokenExpiry(accessToken); // Access Token 만료 시간 가져오기

        String email = (String) userInfo.get("email");

        // GitHub의 경우 기본 API 응답에서 이메일이 제공되지 않으므로, 별도의 API 호출로 가져옴
        if ("github".equals(provider) && (email == null || email.isEmpty())) {
            email = getPrimaryEmailFromGitHub(accessToken);
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("❌ GitHub 이메일 정보를 가져올 수 없습니다.");
            }
        }

        // JWT 생성 (OAuth 만료 시간과 동기화)
        String jwtToken = jwtTokenProvider.createToken(email, expiresAt);
        userInfo.put("jwtToken", jwtToken);
        userInfo.put("email", email);

        logger.info("✅ {} 로그인 성공 - JWT 발급 완료: {}", provider, jwtToken);

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), userInfo, "email");
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
