package com.goorm.team9.icontact.sociallogin.security.jwt;

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
import com.goorm.team9.icontact.sociallogin.security.jwt.JwtBlacklist;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, JwtBlacklist jwtBlacklist) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtBlacklist = jwtBlacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request); // 공통 메서드 사용;

        if (token != null) {
            if (jwtBlacklist.isBlacklisted(token)) { // 블랙리스트 확인 후 차단
                logger.warn("🚨 차단된 토큰 사용 시도: {}", token);
                SecurityContextHolder.clearContext(); // 블랙리스트된 토큰이면 즉시 SecurityContext 초기화
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"로그아웃된 토큰입니다.\"}");
                response.getWriter().flush();
                return;
            }

            if (jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("✅ JWT 인증 성공: {}", authentication.getName());
            } else {
                logger.warn("🛑 유효하지 않은 토큰: {}", token);
            }
        }
        filterChain.doFilter(request, response);
    }
}