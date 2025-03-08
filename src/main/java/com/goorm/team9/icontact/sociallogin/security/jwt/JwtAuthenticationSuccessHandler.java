package com.goorm.team9.icontact.sociallogin.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationSuccessHandler.class);

    public JwtAuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String email = authentication.getName(); // OAuth 로그인한 사용자 이메일 가져오기
        String jwtToken = jwtTokenProvider.createToken(email); // JWT 생성

        // Authorization 헤더로 JWT 반환
        response.setHeader("Authorization", "Bearer " + jwtToken);
        // JWT를 JSON 형식으로 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> tokenResponse = new HashMap<>();
        tokenResponse.put("accessToken", jwtToken);

        response.getWriter().write(new ObjectMapper().writeValueAsString(tokenResponse));
        logger.info("✅ 로그인 성공: {}", email);

        // 홈으로 리다이렉트
        response.sendRedirect("/auth/home");
    }
}
