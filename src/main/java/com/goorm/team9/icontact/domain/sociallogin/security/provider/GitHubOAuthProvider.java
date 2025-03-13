package com.goorm.team9.icontact.domain.sociallogin.security.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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

    @Value("${GITHUB_REDIRECT_URI}")
    private String githubRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate(); // REST API 요청을 보낼 객체 생성
    private static final Logger logger = LoggerFactory.getLogger(GitHubOAuthProvider.class);

    // 사용된 OAuth code 저장 (중복 방지)
    private static final Set<String> usedCodes = new HashSet<>();

    /**
     * GitHub에서 사용자 정보를 가져오는 메서드
     * - GitHub에 `code`를 보내 `access_token` 요청
     * - GitHub에 `access_token`을 보내 `user 정보` 요청
     */
    public Map<String, Object> getUserInfo(String code) {
        logger.info("🔄 GitHub OAuth 인증 요청 시작. 받은 코드: {}", code);

        // 여기서 로그 확인!
        if (code == null || code.isBlank()) {
            logger.error("❌ GitHub 인증 실패: code 값이 없습니다.");
            throw new RuntimeException("GitHub 인증 실패: code 값이 없습니다.");
        }

        // GitHub에서 액세스 토큰 요청
        String accessToken = fetchAccessTokenFromGitHub(code);

        // 응답 로그 추가 (accessToken 확인)
        logger.info("🔍 발급된 GitHub accessToken: {}", accessToken);

        // 액세스 토큰을 이용하여 사용자 정보 요청
        return fetchUserInfoFromGitHub(accessToken);
    }

    /**
     * GitHub에서 액세스 토큰 요청
     */
    private String fetchAccessTokenFromGitHub(String code) {
        logger.info("🔄 GitHub 액세스 토큰 요청 시작: code={}, client_id={}, redirect_uri={}", code, clientId, githubRedirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("client_id", clientId);
        tokenRequest.add("client_secret", clientSecret);
        tokenRequest.add("code", code);
        tokenRequest.add("redirect_uri", githubRedirectUri); // 추가

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

        // `error` 필드가 있는지 확인
        if (responseBody.containsKey("error")) {
            logger.error("❌ GitHub 액세스 토큰 발급 실패: 응답={}", responseBody);
            throw new RuntimeException("GitHub 액세스 토큰을 가져오지 못했습니다. 오류: " + responseBody.get("error_description"));
        }

        String accessToken = (String) responseBody.get("access_token");
        logger.info("🔑 GitHub 액세스 토큰 발급 완료: {}", accessToken);

        // 사용된 code 저장
        markCodeAsUsed(code);

        return accessToken;
    }

    /**
     * GitHub에서 사용자 정보 요청
     */
    /**
     * GitHub에서 사용자 정보 요청
     */
    private Map<String, Object> fetchUserInfoFromGitHub(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://api.github.com/user", HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<>() {});

            Map<String, Object> userInfo = response.getBody();

            if (userInfo == null || userInfo.isEmpty()) {
                logger.error("❌ GitHub 사용자 정보 조회 실패: 응답이 비어 있음");
                throw new RuntimeException("GitHub 사용자 정보를 가져오지 못했습니다.");
            }

            // 불변성을 유지하기 위해 새로운 Map 객체 생성
            Map<String, Object> userInfoWithToken = new HashMap<>(userInfo);
            userInfoWithToken.put("access_token", accessToken);

            logger.info("✅ GitHub 사용자 정보: {}", userInfoWithToken);
            return userInfoWithToken;
        } catch (RestClientException e) {
            logger.error("❌ GitHub 사용자 정보 요청 실패: {}", e.getMessage(), e);
            throw new RuntimeException("GitHub API 요청 중 오류 발생", e);
        }
    }

    public boolean isCodeAlreadyUsed(String code) {
        if (usedCodes.contains(code)) {
            logger.warn("🚫 이미 사용된 OAuth code: {}", code);
            return true;
        }
        return false;
    }

    private void markCodeAsUsed(String code) {
        usedCodes.add(code);
    }
}
