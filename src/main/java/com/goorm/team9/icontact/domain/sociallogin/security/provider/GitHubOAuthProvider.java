package com.goorm.team9.icontact.domain.sociallogin.security.provider;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GitHubOAuthProvider implements OAuthProvider {

    @Value("${GITHUB_CLIENT_ID}")
    private String clientId;

    @Value("${GITHUB_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${GITHUB_REDIRECT_URI}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * GitHub에서 액세스 토큰 요청
     */
    @Override
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("client_id", clientId);
        tokenRequest.add("client_secret", clientSecret);
        tokenRequest.add("code", code);
        tokenRequest.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(tokenRequest, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://github.com/login/oauth/access_token",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {});

        return (String) response.getBody().get("access_token");
    }

    /**
     * GitHub에서 사용자 정보 요청
     */
    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {});

        return response.getBody();
    }

    @Override
    public long getTokenExpiry(String accessToken) {
        HttpHeaders headers = new HttpHeaders();

        // Basic Auth 인증 정보 생성 (client_id:client_secret Base64 인코딩)
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = "https://api.github.com/applications/" + clientId + "/token";

        // 요청 Body 생성 (JSON 형태)
        Map<String, String> body = Map.of("access_token", accessToken);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<>() {}
            );


            // 응답 데이터 확인
            if (response.getBody() == null) {
                throw new RuntimeException("❌ 응답이 null임!");
            }

            // 🔥 expires_in 필드가 없을 수도 있음 → 기본값으로 Long.MAX_VALUE 반환
            Object expiresInObj = response.getBody().get("expires_in");
            if (expiresInObj == null) {
                return Long.MAX_VALUE; // GitHub OAuth 토큰은 기본적으로 만료되지 않으므로 최댓값 반환
            }

            int expiresIn = Integer.parseInt(expiresInObj.toString()); // 안전한 변환 처리
            return System.currentTimeMillis() + (expiresIn * 1000L); // 밀리초 변환 후 반환
        } catch (Exception e) {
            throw new RuntimeException("❌ Github Access Token 만료 시간 조회 실패!", e);
        }
    }

}
