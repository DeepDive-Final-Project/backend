package com.goorm.team9.icontact.sociallogin.service;

import com.goorm.team9.icontact.sociallogin.security.jwt.JwtTokenProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GithubLoginStrategy {

    private static final Logger logger = LoggerFactory.getLogger(GithubLoginStrategy.class);

    private final JwtTokenProvider jwtTokenProvider;

    public GithubLoginStrategy(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String authenticate(String code) {
        logger.info("🔹 GitHub 인증 코드: {}", code);  // ✅ 이게 찍히는지 확인
        // GitHub OAuth 코드로 액세스 토큰 요청
        String accessToken = getAccessTokenFromGitHub(code);
        OAuth2User oAuth2User = loadUserFromGitHub(accessToken);
        logger.info("🔹 GitHub API 응답: {}", oAuth2User.getAttributes());  // ✅ 응답 확인
        // JWT 발급
        String email = (String) oAuth2User.getAttributes().get("email");
        return jwtTokenProvider.createToken(email);
    }

    private String getAccessTokenFromGitHub(String code) {
        String tokenUrl = "https://github.com/login/oauth/access_token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        Map<String, String> body = new HashMap<>();
        body.put("client_id", "Ov23liz6HPq0e9dtvnKS");  // GitHub OAuth 클라이언트 ID
        body.put("client_secret", "d971d4ecfeb0b598399510899d44a3a258b8357d");  // GitHub OAuth 클라이언트 시크릿
        body.put("code", code);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    private OAuth2User loadUserFromGitHub(String accessToken) {
        String userUrl = "https://api.github.com/user";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(
                userUrl,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userAttributes = response.getBody();
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                userAttributes,
                "id"
        );
    }


}

