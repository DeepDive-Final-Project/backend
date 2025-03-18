package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * OAuth2 인증 성공 후 JWT를 발급하는 핸들러.
 * - JWT를 HTTP 헤더 및 JSON 응답으로 클라이언트에게 반환.
 * - 필요 시 클라이언트를 특정 URL로 리다이렉트 가능.
 */
@Component
public class JwtAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationSuccessHandler.class);
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        String email = authentication.getName(); // OAuth 로그인한 사용자 이메일

        // JWT 생성 전 email 값 검증 추가
        if (email == null || "no-email".equals(email)) {
            logger.error("❌ JWT 발급 실패: 유효한 이메일 정보가 없습니다.");

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 응답

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "JWT 발급 실패: 유효한 이메일 정보가 없습니다.");

            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return; // 예외를 던지지 않고 여기서 종료
        }

        // OAuth 인증된 사용자에게 JWT 생성 (기본 만료 시간: 1시간)
        long expiresAt = System.currentTimeMillis() + 3600000;
        String jwtToken = jwtTokenProvider.createToken(email, expiresAt);

        setAuthorizationHeader(response, jwtToken);
        writeJsonResponse(response, jwtToken);

        logger.info("✅ 생성된 JWT 토큰: {}", jwtToken);

        // 필요 시 특정 페이지로 리다이렉트하도록, 지금은 기본 처리 유지
        clearAuthenticationAttributes(request);
    }

    /**
     * JWT를 Authorization 헤더에 추가.
     */
    private void setAuthorizationHeader(HttpServletResponse response, String jwtToken) {
        response.setHeader("Authorization", "Bearer " + jwtToken);
    }

    /**
     * JSON 응답을 클라이언트에게 전송.
     */
    private void writeJsonResponse(HttpServletResponse response, String jwtToken) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> tokenResponse = new HashMap<>();
        tokenResponse.put("accessToken", jwtToken);

        response.getWriter().write(new ObjectMapper().writeValueAsString(tokenResponse));
    }
}
