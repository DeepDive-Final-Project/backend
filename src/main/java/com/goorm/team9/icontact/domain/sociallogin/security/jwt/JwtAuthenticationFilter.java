package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT를 검증하고 SecurityContext에 인증 정보를 설정하는 필터.
 * - 요청에서 JWT를 추출하여 유효성 검증.
 * - 블랙리스트에 등록된 토큰은 차단.
 */
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

            // ecurityContextHolder에 Authentication 설정 추가
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("✅ SecurityContext에 저장된 사용자: " + authentication.getName());
        }
        logger.info("📥 들어온 요청: {} {}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }

    /**
     * JWT를 검증하고 인증 정보를 SecurityContext에 저장.
     */
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

    /**
     * 유효하지 않거나 블랙리스트에 등록된 토큰 처리.
     */
    private void handleInvalidToken(HttpServletResponse response, String logMessage, String token) throws IOException {
        logger.warn("{}: {}", logMessage, token);
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"유효하지 않은 토큰입니다.\"}");
        response.getWriter().flush();
    }
}
