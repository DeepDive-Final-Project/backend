package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthTokenResponse;
import com.goorm.team9.icontact.domain.sociallogin.security.jwt.JwtBlacklist;
import com.goorm.team9.icontact.domain.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProvider;
import com.goorm.team9.icontact.domain.sociallogin.security.provider.OAuthProviderFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스.
 * 로그인, 로그아웃, 회원 탈퇴, JWT 검증을 담당.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuthProviderFactory providerFactory;
    private final OAuthService oAuthService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;
    private final ClientRepository clientRepository;
    private final LoginHistoryService loginHistoryService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * 로그인 처리 (OAuth2.0 인증 후 JWT 발급)
     * 이름 바꾸기 귀찮아서 Github인데 공용입니다.
     *
     * @param code 발급한 인증 코드
     * @return JWT 토큰
     */
    public String loginWithOAuth(String provider, String code) {
        // OAuth Access Token + 사용자 이메일 가져오기
        OAuthTokenResponse tokenResponse = oAuthService.authenticateWithOAuth(provider, code);
        OAuthProvider oAuthProvider = providerFactory.getProvider(provider);

        String accessToken = oAuthService.getAccessToken(provider, code);

        Map<String, Object> userInfo = oAuthProvider.getUserInfo(accessToken);
        String oauthUserId = userInfo.get("id").toString();

        oAuthService.saveOrUpdateUser(provider, tokenResponse.getEmail(), accessToken, oauthUserId);

        String email = tokenResponse.getEmail();
        long oauthTokenExpiryMillis = tokenResponse.getExpiresAt(); // OAuth Access Token 만료 시간 가져오기

        // 사용자 정보 조회 (없으면 예외 발생)
        ClientEntity clientEntity = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 로그인 이력 저장
        loginHistoryService.saveLoginHistory(clientEntity, provider);

        // JWT 발급 (OAuth Access Token 만료 시간 고려)
        String jwtToken = jwtTokenProvider.createToken(email, oauthTokenExpiryMillis);
        logger.info("🔑 발급된 JWT 토큰: {}", jwtToken);

        return jwtToken;
    }

    /**
     * 로그아웃 처리 (JWT 블랙리스트 추가 및 세션 무효화)
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }

        String email = authentication.getName();
        String token = jwtTokenProvider.resolveToken(request);

        if (token != null) {
            jwtBlacklist.addToBlacklist(token, jwtTokenProvider.getExpirationMillis(token));
            oAuthService.invalidateAccessToken(email);
        }

        SecurityContextHolder.clearContext();
        invalidateSession(request);
        clearCookie(response);
    }

    /**
     * 블랙리스트에 등록된 토큰인지 확인
     */
    public boolean isTokenBlacklisted(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return jwtBlacklist.isBlacklisted(token);
    }

    /**
     * 회원 탈퇴 처리 (소프트 삭제 적용)
     */
    public ResponseEntity<String> withdraw(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        String email = authentication.getName();

        // 14일 이내 재탈퇴 불가 검증 추가
        if (!userService.canReRegister(email)) {
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST)
                    .body("탈퇴 후 14일 이내에는 재탈퇴할 수 없습니다.");
        }

        userService.deleteUserByEmail(email);
        logout(request, response);

        return ResponseEntity.ok("회원 탈퇴 완료 ✅");
    }

    /**
     * 세션 무효화
     */
    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * JWT 쿠키 제거
     */
    private void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("Authorization", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
