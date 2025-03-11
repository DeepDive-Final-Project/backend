package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import com.goorm.team9.icontact.domain.sociallogin.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 로그아웃 핸들러.
 * - 로그아웃 시 JWT 블랙리스트 추가 및 OAuth 액세스 토큰 무효화.
 * - 클라이언트가 JWT를 삭제하도록 응답 처리.
 */
@Component
@RequiredArgsConstructor
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtLogoutSuccessHandler.class);

    private final OAuthService oAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        String token = jwtTokenProvider.resolveToken(request);
        if (token != null) {
            long expirationMillis = jwtTokenProvider.getExpirationMillis(token);
            jwtBlacklist.addToBlacklist(token, expirationMillis);
            logger.info("🚫 JWT 블랙리스트 추가 완료: {}", token);
        }

        if (authentication != null) {
            String email = authentication.getName();
            oAuthService.invalidateAccessToken(email);
            logger.info("✅ 로그아웃 완료 - accessToken 제거: {}", email);
        }

        clearClientCookies(response);
        writeResponse(response);
    }

    /**
     * 클라이언트가 저장한 JWT 쿠키를 삭제하도록 설정.
     */
    private void clearClientCookies(HttpServletResponse response) {
        response.addCookie(createExpiredCookie("Authorization"));
    }

    /**
     * 만료된 쿠키 생성 (즉시 삭제됨).
     */
    private jakarta.servlet.http.Cookie createExpiredCookie(String name) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }

    /**
     * JSON 로그아웃 응답을 클라이언트에 전송.
     */
    private void writeResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\": \"로그아웃 완료 ✅\"}");
        response.getWriter().flush();
    }
}
