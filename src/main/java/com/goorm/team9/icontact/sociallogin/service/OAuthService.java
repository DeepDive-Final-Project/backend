package com.goorm.team9.icontact.sociallogin.service;

import com.goorm.team9.icontact.sociallogin.domain.OAuth;
import com.goorm.team9.icontact.sociallogin.domain.User;
import com.goorm.team9.icontact.sociallogin.repository.OAuthRepository;
import com.goorm.team9.icontact.sociallogin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;

    @Transactional
    public User saveOrUpdateUser(String provider, String oauthUserId, String email, String nickname, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        Optional<OAuth> existingOAuth = oauthRepository.findByProviderAndOauthUserId(provider, oauthUserId);

        if (existingOAuth.isEmpty()) {
            // User 테이블에 이미 존재하는지 확인
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .nickname(nickname)
                                    .email(email)
                                    .age(25L)  // 기본값 설정
                                    .industry(null) // 필요시 설정
                                    .role(null) // 필요시 설정
                                    .career(null) // 필요시 설정
                                    .status(null) // 필요시 설정
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

            // OAuth 정보 저장
            OAuth oauth = OAuth.builder()
                    .provider(provider)
                    .oauthUserId(oauthUserId)
                    .email(email)
                    .user(user)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresAt(expiresAt)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            oauthRepository.save(oauth);
        }

        return userRepository.findByEmail(email).orElseThrow();
    }
    @Transactional
    public void invalidateAccessToken(String email) {
        oauthRepository.findByEmail(email).ifPresent(oauth -> {
            System.out.println("🛑 [로그아웃] 기존 accessToken: " + oauth.getAccessToken());
            oauth.updateAccessToken(null); // accessToken을 null로 변경
            oauthRepository.save(oauth);
            System.out.println("✅ [로그아웃] accessToken 제거 완료! DB 업데이트 확인 필요");
        });
    }

}
