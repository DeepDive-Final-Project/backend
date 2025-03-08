package com.goorm.team9.icontact.sociallogin.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.goorm.team9.icontact.sociallogin.security.jwt.JwtBlacklist;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, JwtBlacklist jwtBlacklist) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtBlacklist = jwtBlacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request); // 공통 메서드 사용;
        System.out.println("🔍 [JWT 필터] 요청 헤더에서 추출한 토큰: " + token);

        if (token != null) {
            if (jwtBlacklist.isBlacklisted(token)) { // 블랙리스트 확인 후 차단
                System.out.println("🚨 차단된 토큰 사용 시도: " + token);
                SecurityContextHolder.clearContext(); // 블랙리스트된 토큰이면 즉시 SecurityContext 초기화
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"로그아웃된 토큰입니다.\"}");
                response.getWriter().flush();
                return;
            }

            if (jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("✅ [JWT 필터] SecurityContext에 인증 정보 저장 완료");
            } else {
                System.out.println("🛑 [JWT 필터] 유효하지 않거나 블랙리스트에 등록된 토큰! 인증 불가");
            }
        }

        filterChain.doFilter(request, response);
    }
}