/**
 *  구글 로그인 구현
 */
//package com.goorm.team9.icontact.domain.sociallogin.security.provider;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//import java.util.Map;
//
//@Service
//public class GoogleOAuthProvider implements OAuthProvider {
//
//    @Value("${GOOGLE_CLIENT_ID}")
//    private String clientId;
//
//    @Value("${GOOGLE_CLIENT_SECRET}")
//    private String clientSecret;
//
//    @Value("${GOOGLE_REDIRECT_URI}")
//    private String redirectUri;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    @Override
//    public Map<String, Object> getUserInfo(String code) {
//        String accessToken = fetchAccessTokenFromGoogle(code);
//        return fetchUserInfoFromGoogle(accessToken);
//    }
//
//    private String fetchAccessTokenFromGoogle(String code) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.set("Accept", "application/json");
//
//        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
//        tokenRequest.add("client_id", clientId);
//        tokenRequest.add("client_secret", clientSecret);
//        tokenRequest.add("code", code);
//        tokenRequest.add("redirect_uri", redirectUri);
//        tokenRequest.add("grant_type", "authorization_code");
//
//        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(tokenRequest, headers);
//        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
//                "https://oauth2.googleapis.com/token",
//                HttpMethod.POST,
//                requestEntity,
//                new ParameterizedTypeReference<>() {});
//
//        return (String) response.getBody().get("access_token");
//    }
//
//    private Map<String, Object> fetchUserInfoFromGoogle(String accessToken) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        headers.set("Accept", "application/json");
//
//        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
//        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
//                "https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, requestEntity,
//                new ParameterizedTypeReference<>() {});
//
//        return response.getBody();
//    }
//}
//
