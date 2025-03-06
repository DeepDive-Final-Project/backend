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

@Component
public class GithubLoginStrategy {
    private final JwtTokenProvider jwtTokenProvider;

    public GithubLoginStrategy(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String authenticate(String code) {
        // GitHub OAuth 코드로 액세스 토큰 요청
        String accessToken = getAccessTokenFromGitHub(code);
        OAuth2User oAuth2User = loadUserFromGitHub(accessToken);

        // JWT 발급
        String email = (String) oAuth2User.getAttributes().get("email");
        return jwtTokenProvider.createToken(email);
    }

    private String getAccessTokenFromGitHub(String code) {
        String tokenUrl = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        Map<String, String> body = new HashMap<>();
        body.put("client_id", "your-client-id");  // GitHub OAuth 클라이언트 ID
        body.put("client_secret", "your-client-secret");  // GitHub OAuth 클라이언트 시크릿
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

