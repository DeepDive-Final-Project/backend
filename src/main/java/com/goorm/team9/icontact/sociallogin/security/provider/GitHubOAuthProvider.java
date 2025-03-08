package com.goorm.team9.icontact.sociallogin.security.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class GitHubOAuthProvider {

    private final RestTemplate restTemplate = new RestTemplate(); // REST API 요청을 보낼 객체 생성
    private static final Logger logger = LoggerFactory.getLogger(GitHubOAuthProvider.class);

    public Map<String, Object> getUserInfo(String accessToken) {  // Access Token을 사용해 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);  // Authorization 헤더에 Access Token 추가

        HttpEntity<String> entity = new HttpEntity<>(headers); // 요청 헤더만 포함한 HTTP 요청 객체 생성
        ResponseEntity<Map> response = restTemplate.exchange(  // GitHub API 호출
                "https://api.github.com/user",  // GitHub API의 유저 정보 엔드포인트
                HttpMethod.GET,  // GET 방식으로 요청
                entity,  // 요청 헤더 포함
                Map.class  // 응답을 Map 형태로 변환해서 받음
        );
        logger.info("✅ GitHub 사용자 정보 조회 성공");
        return response.getBody();  // 응답 바디를 반환 (GitHub 유저 정보)
    }
}

