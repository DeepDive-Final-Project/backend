/**
 *  카카오 로그인 구현
 */
package com.goorm.team9.icontact.domain.sociallogin.security.provider;

import com.goorm.team9.icontact.domain.sociallogin.service.CustomOAuth2UserService;
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
import org.springframework.web.client.HttpClientErrorException;
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
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

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
    public Map<String, Object> getUserInfo(String accessToken) {
        logger.info("🔍 카카오 Access Token 사용: {}", accessToken);
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

        logger.info("🔍 카카오 인가 코드 요청: {}", code);  // 로그 추가

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(tokenRequest, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            logger.info("✅ 카카오 Access Token 응답: {}", response.getBody());

            return (String) response.getBody().get("access_token");
        } catch (HttpClientErrorException.BadRequest e) {
            logger.error("❌ 카카오 Access Token 요청 실패: {}", e.getResponseBodyAsString());
            throw new RuntimeException("❌ 카카오 Access Token 요청 실패: " + e.getResponseBodyAsString());
        }
    }

    private Map<String, Object> fetchUserInfoFromKakao(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("❌ 카카오 사용자 정보 조회 실패: 응답이 비어 있음");
            }

            logger.info("✅ 카카오 사용자 정보: {}", responseBody);

            // 사용자 정보에서 이메일 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) responseBody.get("kakao_account");
            if (kakaoAccount == null) {
                throw new RuntimeException("❌ 카카오 계정 정보를 찾을 수 없음");
            }

            // 이메일 가져오기 (동의하지 않았다면 null일 수 있음)
            String email = (String) kakaoAccount.get("email");

            // 이메일이 null이면 예외 발생 대신 기본 처리
            if (email == null) {
                logger.warn("⚠️ 카카오 로그인 사용자 이메일 정보가 제공되지 않음");
            } else {
                logger.info("📧 카카오 사용자 이메일: {}", email);
            }

            // 반환 데이터에 email 추가
            responseBody.put("email", email);
            return responseBody;

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new RuntimeException("❌ Access Token이 만료되었습니다. 다시 로그인하세요!");
        } catch (HttpClientErrorException.Forbidden e) {
            throw new RuntimeException("❌ Access Token이 유효하지 않습니다!");
        }
    }


    @Override
    public long getTokenExpiry(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v1/user/access_token_info",
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            // Object 타입으로 가져온 후, String -> Integer 변환
            Object expiresInObj = response.getBody().get("expires_in");
            int expiresIn = Integer.parseInt(expiresInObj.toString()); // 안전한 변환 처리
            return System.currentTimeMillis() + (expiresIn * 1000L); // 밀리초 변환 후 반환
        } catch (Exception e) {
            throw new RuntimeException("❌ Kakao Access Token 만료 시간 조회 실패!", e);
        }
    }

}

