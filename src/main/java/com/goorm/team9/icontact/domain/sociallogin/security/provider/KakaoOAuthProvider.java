/**
 *  카카오 로그인 구현
 */
package com.goorm.team9.icontact.domain.sociallogin.security.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class KakaoOAuthProvider implements OAuthProvider {

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${KAKAO_REDIRECT_URI}")
    private String kakaoRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("grant_type", "authorization_code");
        tokenRequest.add("client_id", clientId);
        tokenRequest.add("client_secret", clientSecret);
        tokenRequest.add("redirect_uri", kakaoRedirectUri);
        tokenRequest.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(tokenRequest, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {});

        return (String) response.getBody().get("access_token");
    }

    @Override
    public Map<String, Object> getUserInfo(String code) {
        String accessToken = fetchAccessTokenFromKakao(code);
        return fetchUserInfoFromKakao(accessToken);
    }

    private String fetchAccessTokenFromKakao(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("client_id", clientId);
        tokenRequest.add("client_secret", clientSecret);
        tokenRequest.add("code", code);
        tokenRequest.add("redirect_uri", kakaoRedirectUri);
        tokenRequest.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(tokenRequest, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {});

        return (String) response.getBody().get("access_token");
    }

    private Map<String, Object> fetchUserInfoFromKakao(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<>() {});

        return response.getBody();
    }
}

