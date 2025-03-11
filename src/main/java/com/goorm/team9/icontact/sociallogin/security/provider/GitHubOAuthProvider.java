package com.goorm.team9.icontact.sociallogin.security.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
     * GitHub에서 인증된 사용자 정보를 가져오는 메서드
     *
     * @return 사용자 정보 (JSON 형식의 Map)
     */
    public Map<String, Object> getUserInfo(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // GitHub에서 access_token 받기
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.set("Accept", "application/json");

        Map<String, String> tokenRequest = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code
        );

        HttpEntity<Map<String, String>> tokenEntity = new HttpEntity<>(tokenRequest, tokenHeaders);
        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                "https://github.com/login/oauth/access_token", HttpMethod.POST, tokenEntity,
                new ParameterizedTypeReference<>() {
                });

        // ✅ 액세스 토큰 값 확인 로그 추가
        Map<String, Object> responseBody = tokenResponse.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            logger.error("❌ GitHub에서 액세스 토큰을 받지 못함: {}", responseBody);
            throw new RuntimeException("GitHub 액세스 토큰 발급 실패!");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");
        logger.info("🔑 액세스 토큰 발급 완료: {}", accessToken);

        // access_token을 사용하여 사용자 정보 요청
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("Authorization", "Bearer " + accessToken);
        userHeaders.set("Accept", "application/json");

        HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                "https://api.github.com/user", HttpMethod.GET, userEntity,
                new ParameterizedTypeReference<>() {
                });

        Map<String, Object> userInfo = userResponse.getBody();
        userInfo.put("access_token", accessToken); // 사용자 정보에 access_token 추가
        logger.info("✅ GitHub 사용자 정보: {}", userInfo);

        return userInfo;
    }
}
