package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProviderFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

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
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("❌ 이메일 정보가 없습니다.");
        }

        // JWT 생성 (OAuth 만료 시간과 동기화)
        String jwtToken = jwtTokenProvider.createToken(email, expiresAt);
        userInfo.put("jwtToken", jwtToken);

        logger.info("✅ {} 로그인 성공 - JWT 발급 완료: {}", provider, jwtToken);

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), userInfo, "email");
    }
}
