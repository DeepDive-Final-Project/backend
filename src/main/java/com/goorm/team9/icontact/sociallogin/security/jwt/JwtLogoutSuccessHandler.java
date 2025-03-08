package com.goorm.team9.icontact.sociallogin.security.jwt;

import com.goorm.team9.icontact.sociallogin.service.OAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    private final OAuthService oAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;
    private static final Logger logger = LoggerFactory.getLogger(JwtLogoutSuccessHandler.class);

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        String token = jwtTokenProvider.resolveToken(request); // 요청에서 JWT 추출
        if (token != null) {
            long expirationMillis = jwtTokenProvider.getExpirationMillis(token); // 토큰 만료 시간 가져오기
            jwtBlacklist.addToBlacklist(token, expirationMillis); // 블랙리스트에 추가
            logger.info("🚫 JWT 블랙리스트 추가 완료: {}", token);
        }

        if (authentication != null) {
            String email = authentication.getName();
            oAuthService.invalidateAccessToken(email); // DB에서도 accessToken 제거
            logger.info("✅ 로그아웃 완료 - accessToken 제거: {}", email);
        } else {
            logger.warn("⚠️ 로그아웃 시 인증 정보가 없음");
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\": \"로그아웃 완료 ✅\"}");
        response.getWriter().flush();
    }
}
