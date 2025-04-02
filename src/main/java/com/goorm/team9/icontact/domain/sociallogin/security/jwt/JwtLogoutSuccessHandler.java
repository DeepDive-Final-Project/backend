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
        if (token != null && jwtTokenProvider.validateToken(token)) {
            long expirationMillis = jwtTokenProvider.getExpirationMillis(token);
            jwtBlacklist.addToBlacklist(token, expirationMillis);
            logger.info("🚫 JWT 블랙리스트 추가 완료: {}", token);
        }

        SecurityContextHolder.clearContext();

        if (authentication != null && authentication.getName() != null) {
            String email = authentication.getName();

            logger.info("🔍 OAuth 로그아웃 시 accessToken 제거 요청: {}", email);
            oAuthService.invalidateAccessToken(email);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\": \"로그아웃 완료 ✅\"}");
        response.getWriter().flush();
    }

}
