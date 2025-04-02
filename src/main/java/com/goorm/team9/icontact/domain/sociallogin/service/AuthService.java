package com.goorm.team9.icontact.domain.sociallogin.service;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthLoginResponseDto;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthTokenResponseDto;
import com.goorm.team9.icontact.domain.sociallogin.security.jwt.JwtBlacklist;
import com.goorm.team9.icontact.domain.sociallogin.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuthService oAuthService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;
    private final ClientRepository clientRepository;
    private final LoginHistoryService loginHistoryService;

    public OAuthLoginResponseDto loginWithOAuth(String provider, String code, HttpServletRequest request) {
        OAuthTokenResponseDto tokenResponse = oAuthService.authenticateWithOAuth(provider, code);
        String email = tokenResponse.getEmail();
        long expiresAt = tokenResponse.getExpiresAt();
        boolean isNewUser = !clientRepository.existsByEmailAndProvider(email, provider);

        ClientEntity clientEntity = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String nickname = clientEntity.getNickName();
        String accessToken = jwtTokenProvider.createToken(email, expiresAt, provider, nickname);
        loginHistoryService.saveLoginHistory(clientEntity, provider);

        return new OAuthLoginResponseDto(
                email,
                provider,
                accessToken,
                null,
                clientEntity.getRole().toString(),
                isNewUser,
                clientEntity.getNickName()
        );
    }


    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }

        String token = jwtTokenProvider.resolveToken(request);

        if (token != null) {
            jwtBlacklist.addToBlacklist(token, jwtTokenProvider.getExpirationMillis(token));
        }

        SecurityContextHolder.clearContext();
        invalidateSession(request);
        clearCookie(response);
    }

    public boolean isTokenBlacklisted(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return jwtBlacklist.isBlacklisted(token);
    }

    public ResponseEntity<String> withdraw(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        String email = authentication.getName();
        String token = jwtTokenProvider.resolveToken(request);
        String provider = null;

        if (authentication instanceof OAuth2AuthenticationToken oAuth2Token) {
            provider = oAuth2Token.getAuthorizedClientRegistrationId();
        }

        if (provider == null && token != null) {
            provider = jwtTokenProvider.getProvider(token);
        }

        Optional<ClientEntity> clientOpt = clientRepository.findByEmailAndProviderAndIsDeletedFalse(email, provider);
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND)
                    .body("탈퇴한 사용자이거나 존재하지 않는 사용자입니다.");
        }

        if (!userService.canReRegister(email, provider)) {
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST)
                    .body("탈퇴 후 14일 이내에는 재탈퇴할 수 없습니다.");
        }

        userService.deleteUserByEmail(email, provider);
        logout(request, response);

        return ResponseEntity.ok("회원 탈퇴 완료 ✅");
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("Authorization", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
