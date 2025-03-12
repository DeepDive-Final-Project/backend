package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import com.goorm.team9.icontact.domain.sociallogin.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 로그아웃 핸들러.
 * - 로그아웃 시 JWT를 블랙리스트에 추가하여 차단.
 * - SecurityContext 초기화.
 * - JSON 응답 반환.
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

        // JWT 토큰 추출 및 블랙리스트 추가
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {  // 만료된 토큰 제외
            long expirationMillis = jwtTokenProvider.getExpirationMillis(token);
            jwtBlacklist.addToBlacklist(token, expirationMillis);
            logger.info("🚫 JWT 블랙리스트 추가 완료: {}", token);
        }

        // SecurityContext 클리어
        SecurityContextHolder.clearContext();

        // OAuth 토큰 무효화 (필요한 경우만)
        if (authentication != null && authentication.getName() != null) {
            String email = authentication.getName();
            oAuthService.invalidateAccessToken(email);
            logger.info("✅ 로그아웃 완료 - accessToken 제거: {}", email);
        }

        // JSON 응답 반환
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\": \"로그아웃 완료 ✅\"}");
        response.getWriter().flush();
    }
}
