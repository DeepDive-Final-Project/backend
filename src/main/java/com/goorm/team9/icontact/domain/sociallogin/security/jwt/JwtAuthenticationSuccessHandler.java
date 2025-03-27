package com.goorm.team9.icontact.domain.sociallogin.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;

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
    private final ClientRepository clientRepository; // 추가

    public JwtAuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider, ClientRepository clientRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.clientRepository = clientRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String email = authentication.getName();

        boolean isNewUser = !clientRepository.existsByEmailAndProviderAndIsDeletedFalse(email, provider);

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

        Optional<ClientEntity> optionalClient = clientRepository.findByEmailAndProvider(email, provider);

        if (optionalClient.isPresent()) {
            ClientEntity client = optionalClient.get();

            boolean hasWithdrawnRole = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_WITHDRAWN"));

            if (hasWithdrawnRole && client.getDeleted_at() != null) {
                boolean isExpired = client.getDeleted_at().plusDays(14).isBefore(LocalDateTime.now());

                if (isExpired) {
                    logger.warn("❌ 탈퇴 14일 경과 - 복구 불가: {}", email);

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    Map<String, String> expiredResponse = Map.of(
                            "status", "expired",
                            "message", "계정 복구 가능 기간(14일)이 지나 복구가 불가능합니다."
                    );
                    response.getWriter().write(new ObjectMapper().writeValueAsString(expiredResponse));
                    return;
                }

                // 복구 가능한 탈퇴자일 경우 → 복구 페이지 리디렉트
                String redirectUrl = "https://www.i-contacts.link/restore";
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                logger.info("🚫 탈퇴자 리디렉션 완료: {}", redirectUrl);
                return;
            }
        }

        // OAuth 인증된 사용자에게 JWT 생성 (기본 만료 시간: 1시간)
        long expiresAt = System.currentTimeMillis() + 3600000;
        String jwtToken = jwtTokenProvider.createToken(email, expiresAt, provider);

        setAuthorizationHeader(response, jwtToken);
        setJwtCookie(response, jwtToken);
        writeJsonResponse(response, jwtToken);

        logger.info("✅ 생성된 JWT 토큰: {}", jwtToken);

        String redirectUrl = isNewUser
                ? "https://www.i-contacts.link/profile1"
                : "https://www.i-contacts.link/home";

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        logger.info("✅ 로그인 성공, 토큰 발급 및 리디렉션 완료");
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

    /**
     *  JWT를 브라우저 쿠키에 저장하는 메서드
     */
    private void setJwtCookie(HttpServletResponse response, String jwtToken) {
        Cookie jwtCookie = new Cookie("Authorization", jwtToken);
        jwtCookie.setHttpOnly(true);        // JS로 접근 못 하게 (보안 강화)
        jwtCookie.setSecure(false);         // HTTPS 환경이면 true로 설정
        jwtCookie.setPath("/");             // 모든 경로에서 접근 가능
        jwtCookie.setMaxAge(60 * 60);       // 1시간 유효

        response.addCookie(jwtCookie);
    }

}
