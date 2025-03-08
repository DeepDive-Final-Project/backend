package com.goorm.team9.icontact.sociallogin.service;

import com.goorm.team9.icontact.sociallogin.domain.OAuth;
import com.goorm.team9.icontact.sociallogin.domain.User;
import com.goorm.team9.icontact.sociallogin.repository.OAuthRepository;
import com.goorm.team9.icontact.sociallogin.repository.UserRepository;
import com.goorm.team9.icontact.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.sociallogin.security.provider.GitHubOAuthProvider;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
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
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthService oAuthService;
    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;
    private final JwtTokenProvider jwtTokenProvider; // JWT 발급을 위한 프로바이더 추가
    private final GitHubOAuthProvider gitHubOAuthProvider;
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // Access Token 가져오기
        OAuth2AccessToken accessToken = userRequest.getAccessToken();
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 기존 attributes를 변경 가능한 HashMap으로 변환
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String oauthUserId = attributes.get("id").toString();
        String nickname = (String) attributes.get("login");

        // GitHub API를 호출해서 이메일 가져오기
//        String email = getGitHubEmail(accessToken.getTokenValue());
        Map<String, Object> githubUserInfo = gitHubOAuthProvider.getUserInfo(accessToken.getTokenValue());
        String email = (String) githubUserInfo.getOrDefault("email", "no-email");

        // 사용자 정보 저장 또는 업데이트
        // 중복 제거: OAuthService의 saveOrUpdateUser 메서드 호출
        oAuthService.saveOrUpdateUser(
                provider,
                oauthUserId,
                email,
                nickname,
                accessToken.getTokenValue(),
                "dummy-refresh-token",
                accessToken.getExpiresAt() != null ?
                        LocalDateTime.ofInstant(accessToken.getExpiresAt(), ZoneId.systemDefault()) : null
        );

        // JWT 발급
        String jwtToken = jwtTokenProvider.createToken(email);

        // JWT 추가
        attributes.put("jwtToken", jwtToken);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes, "id"
        );
    }
}