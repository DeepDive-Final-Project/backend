package com.goorm.team9.icontact.sociallogin.service;

import com.goorm.team9.icontact.sociallogin.domain.OAuth;
import com.goorm.team9.icontact.sociallogin.domain.User;
import com.goorm.team9.icontact.sociallogin.repository.OAuthRepository;
import com.goorm.team9.icontact.sociallogin.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // ✅ Access Token 가져오기
        OAuth2AccessToken accessToken = userRequest.getAccessToken();
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "github"
        String oauthUserId = attributes.get("id").toString();
//        String email = (String) attributes.getOrDefault("email", "no-email"); // email이 없을 경우 기본값 설정
        String nickname = (String) attributes.get("login"); // GitHub username
        // ✅ GitHub API를 호출해서 이메일 가져오기
        String email = getGitHubEmail(accessToken.getTokenValue());

        // ✅ Access Token을 포함하여 사용자 저장 또는 업데이트
        saveOrUpdateUser(provider, oauthUserId, email, nickname, accessToken);

        return oAuth2User;
    }

    private void saveOrUpdateUser(String provider, String oauthUserId, String email, String nickname, OAuth2AccessToken accessToken) {
        Optional<OAuth> existingOAuth = oauthRepository.findByProviderAndOauthUserId(provider, oauthUserId);

        if (existingOAuth.isEmpty()) {
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .nickname(nickname)
                                    .email(email)
                                    .age(25L)  // 기본값
                                    .industry(null)
                                    .role(null)
                                    .career(null)
                                    .status(null)
                                    .introduction("")
                                    .link("")
                                    .profileImage(null)
                                    .chatOpportunity(0L)
                                    .chatMessage(0L)
                                    .offline(false)
                                    .isDeleted(false)
                                    .deletedAt(null)
                                    .build()
                    ));

            OAuth oauth = OAuth.builder()
                    .provider(provider)
                    .oauthUserId(oauthUserId)
                    .email(email)
                    .user(user)
                    .accessToken(accessToken.getTokenValue())  // ✅ 실제 Access Token 저장
                    .refreshToken("dummy-refresh-token")  // OAuth 토큰 처리 필요, GitHub OAuth에는 refresh token이 없을 수도 있음
                    .expiresAt(accessToken.getExpiresAt() != null ?
                            LocalDateTime.ofInstant(accessToken.getExpiresAt(), ZoneId.systemDefault()) : null)// ✅ 토큰 만료 시간 저장
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();

            oauthRepository.save(oauth);
        }
    }

    private String getGitHubEmail(String accessToken) {
        String emailEndpoint = "https://api.github.com/user/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List> response = restTemplate.exchange(emailEndpoint, HttpMethod.GET, entity, List.class);

        if (response.getBody() != null) {
            for (Object obj : response.getBody()) {
                Map<String, Object> emailData = (Map<String, Object>) obj;
                if ((boolean) emailData.get("primary")) { // 기본 이메일 선택
                    return (String) emailData.get("email");
                }
            }
        }
        return "no-email"; // 이메일이 없을 경우 기본값
    }
}