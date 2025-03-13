package com.goorm.team9.icontact.domain.sociallogin.security.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * GitHub OAuth Provider
 * - GitHub API와 통신하여 사용자 정보를 가져옴.
 */
@Service
public class GitHubOAuthProvider {

    @Value("${GITHUB_CLIENT_ID}")
    private String clientId;

    @Value("${GITHUB_CLIENT_SECRET}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate(); // REST API 요청을 보낼 객체 생성
    private static final Logger logger = LoggerFactory.getLogger(GitHubOAuthProvider.class);

    /**
     * GitHub에서 사용자 정보를 가져오는 메서드
     * - GitHub에 `code`를 보내 `access_token` 요청
     * - GitHub에 `access_token`을 보내 `user 정보` 요청
     */
    public Map<String, Object> getUserInfo(String code) {
        logger.info("🔄 GitHub OAuth 인증 요청 시작. 받은 코드: {}", code);

        // GitHub에서 액세스 토큰 요청
        String accessToken = fetchAccessTokenFromGitHub(code);

        // 액세스 토큰을 이용하여 사용자 정보 요청
        return fetchUserInfoFromGitHub(accessToken);
    }

    /**
     * GitHub에서 액세스 토큰 요청
     */
    private String fetchAccessTokenFromGitHub(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("client_id", clientId);
        tokenRequest.add("client_secret", clientSecret);
        tokenRequest.add("code", code);

//        Map<String, String> tokenRequest = Map.of(
//                "client_id", clientId,
//                "client_secret", clientSecret,
//                "code", code
//        );

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(tokenRequest, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://github.com/login/oauth/access_token",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {});

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            logger.error("❌ GitHub 액세스 토큰 발급 실패: 응답={}", responseBody);
            throw new RuntimeException("GitHub 액세스 토큰을 가져오지 못했습니다.");
        }

        String accessToken = (String) responseBody.get("access_token");
        logger.info("🔑 GitHub 액세스 토큰 발급 완료: {}", accessToken);
        return accessToken;
    }

    /**
     * GitHub에서 사용자 정보 요청
     */
    private Map<String, Object> fetchUserInfoFromGitHub(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.github.com/user", HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<>() {});

        Map<String, Object> userInfo = response.getBody();
        if (userInfo == null) {
            logger.error("❌ GitHub 사용자 정보 조회 실패!");
            throw new RuntimeException("GitHub 사용자 정보를 가져오지 못했습니다.");
        }

        // access_token도 포함해서 반환
        userInfo.put("access_token", accessToken);
        logger.info("✅ GitHub 사용자 정보: {}", userInfo);
        return userInfo;
    }
}
