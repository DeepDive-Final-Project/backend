package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, JwtBlacklist jwtBlacklist) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtBlacklist = jwtBlacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (List.of("/auth/token-status").contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtTokenProvider.resolveToken(request);
        logger.info("🔍 요청된 JWT: " + token);

        if (token == null) {
            logger.warn("⚠️ JWT 토큰이 없음");
        } else {
            logger.info("🔍 요청된 JWT: " + token);
            if (jwtBlacklist.isBlacklisted(token)) {
                logger.warn("🚨 차단된 JWT 토큰: " + token);
                handleInvalidToken(response, "🚨 차단된 토큰", token);
                return;
            }
            if (!jwtTokenProvider.validateToken(token)) {
                logger.warn("🛑 유효하지 않은 JWT 토큰: " + token);
                handleInvalidToken(response, "🛑 유효하지 않은 토큰", token);
                return;
            }

            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("✅ SecurityContext에 저장된 사용자: " + authentication.getName());
        }
        logger.info("📥 들어온 요청: {} {}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }

    private void processToken(String token, HttpServletResponse response) throws IOException {
        if (jwtBlacklist.isBlacklisted(token)) {
            handleInvalidToken(response, "🚨 차단된 토큰 사용 시도", token);
            return;
        }

        if (jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("✅ JWT 인증 성공: {}", authentication.getName());
        } else {
            handleInvalidToken(response, "🛑 유효하지 않은 토큰", token);
        }
    }

    private void handleInvalidToken(HttpServletResponse response, String logMessage, String token) throws IOException {
        logger.warn("{}: {}", logMessage, token);
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"유효하지 않은 토큰입니다.\"}");
        response.getWriter().flush();
    }

}
