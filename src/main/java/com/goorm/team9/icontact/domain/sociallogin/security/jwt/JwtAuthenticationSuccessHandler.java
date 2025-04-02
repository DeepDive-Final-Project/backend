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

@Component
public class JwtAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationSuccessHandler.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientRepository clientRepository;

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

        if (email == null || "no-email".equals(email)) {
            logger.error("❌ JWT 발급 실패: 유효한 이메일 정보가 없습니다.");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "JWT 발급 실패: 유효한 이메일 정보가 없습니다.");
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return;
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

                String referer = request.getHeader("Referer");
                String baseUrl = "https://www.i-contacts.link";
                if (referer != null) {
                    if (referer.contains("localhost:5173")) {
                        baseUrl = "http://localhost:5173";
                    } else if (referer.contains("www.i-contacts.link")) {
                        baseUrl = "https://www.i-contacts.link";
                    }
                }

                String redirectUrl = baseUrl + "/restore";
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                logger.info("🚫 탈퇴자 리디렉션 완료: {}", redirectUrl);
                return;
            }
        }

        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        boolean isNewUser = Boolean.TRUE.equals(attributes.get("isNewUser"));

        long expiresAt = System.currentTimeMillis() + 3600000;
        String nickname = optionalClient.map(ClientEntity::getNickName).orElse("unknown");
        String jwtToken = jwtTokenProvider.createToken(email, expiresAt, provider, nickname);
        setAuthorizationHeader(response, jwtToken);
        setJwtCookie(request, response, jwtToken);
        writeJsonResponse(response, jwtToken);
        logger.info("✅ 생성된 JWT 토큰: {}", jwtToken);

        String referer = request.getHeader("Referer");
        String baseUrl = "https://www.i-contacts.link";
        if (referer != null) {
            if (referer.contains("localhost:5173")) {
                baseUrl = "http://localhost:5173";
            } else if (referer.contains("www.i-contacts.link")) {
                baseUrl = "https://www.i-contacts.link";
            }
        }

        String redirectUrl = isNewUser
                ? baseUrl + "/profile/1"
                : baseUrl + "/home";

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        logger.info("✅ 로그인 성공, 토큰 발급 및 리디렉션 완료: {}", redirectUrl);
    }

    private void setAuthorizationHeader(HttpServletResponse response, String jwtToken) {
        response.setHeader("Authorization", "Bearer " + jwtToken);
    }

    private void writeJsonResponse(HttpServletResponse response, String jwtToken) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> tokenResponse = new HashMap<>();
        tokenResponse.put("accessToken", jwtToken);

        response.getWriter().write(new ObjectMapper().writeValueAsString(tokenResponse));
    }

    private void setJwtCookie(HttpServletRequest request, HttpServletResponse response, String jwtToken) {
        Cookie jwtCookie = new Cookie("Authorization", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(request.isSecure());
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60);
        jwtCookie.setDomain("i-contacts.link");

        response.addCookie(jwtCookie);
    }

}
