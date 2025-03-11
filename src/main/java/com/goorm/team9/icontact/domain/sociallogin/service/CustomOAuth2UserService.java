package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.GitHubOAuthProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth 로그인 시 사용자 정보를 처리하는 서비스
 * - OAuth2User를 로드하고 JWT를 생성하여 반환
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthService oAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GitHubOAuthProvider gitHubOAuthProvider;
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    /**
     * OAuth2 로그인 후 사용자 정보를 가져오고 JWT 발급
     *
     * @param userRequest OAuth2 로그인 요청 정보
     * @return OAuth2User (JWT 포함)
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2AccessToken accessToken = userRequest.getAccessToken();
        String accessTokenValue = accessToken.getTokenValue();
        Map<String, Object> githubUserInfo = gitHubOAuthProvider.getUserInfo(accessToken.getTokenValue());

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String oauthUserId = githubUserInfo.get("id").toString();
        String email = (String) githubUserInfo.getOrDefault("email", "no-email");
        String nickname = (String) githubUserInfo.get("login");

        // ✅ OAuth 사용자 정보 저장 (OAuthService의 메서드 호출)
        oAuthService.saveOrUpdateUser(provider, oauthUserId, email, nickname, accessTokenValue); // ✅ accessToken 추가

        // ✅ JWT 발급
        String jwtToken = jwtTokenProvider.createToken(email);

        // ✅ 사용자 정보 반환 (JWT 포함)
        Map<String, Object> attributes = new HashMap<>(githubUserInfo);
        attributes.put("jwtToken", jwtToken);

        logger.info("✅ OAuth 로그인 완료: {}, JWT 발급됨", email);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes, "id"
        );
    }
}
