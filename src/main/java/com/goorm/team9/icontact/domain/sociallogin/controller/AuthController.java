package com.goorm.team9.icontact.domain.sociallogin.controller;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.sociallogin.dto.JwtResponse;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthLoginRequest;
import com.goorm.team9.icontact.domain.sociallogin.service.AuthService;
import com.goorm.team9.icontact.domain.sociallogin.service.LoginHistoryService;
import com.goorm.team9.icontact.domain.sociallogin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 사용자 인증 관련 컨트롤러
 * - GitHub 로그인
 * - 로그아웃
 * - 토큰 블랙리스트 확인
 * - 회원 탈퇴
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "사용자 인증 관련 API 입니다.")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final LoginHistoryService loginHistoryService;
    private final ClientRepository clientRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * 서버 상태 확인용 엔드포인트
     */
    @GetMapping("/home")
    @Operation(summary = "서버 상태 확인 API", description = "서버의 상태를 확인하는 API 입니다.")
    public String home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "❌ 인증되지 않음 (401)";
        }
        return "✅ 인증됨: " + authentication.getName();
    }

    @PostMapping("/github")
    @Operation(summary = "GitHub 로그인 API", description = "GitHub OAuth를 사용하여 로그인하고 JWT 토큰을 반환합니다.")
    public ResponseEntity<JwtResponse> loginWithGithub(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(new JwtResponse(authService.loginWithOAuth("github", request.getCode())));
    }

    @PostMapping("/google")
    @Operation(summary = "Google 로그인 API", description = "Google OAuth를 사용하여 로그인하고 JWT 토큰을 반환합니다.")
    public ResponseEntity<JwtResponse> loginWithGoogle(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(new JwtResponse(authService.loginWithOAuth("google", request.getCode())));
    }

    @PostMapping("/kakao")
    @Operation(summary = "Kakao 로그인 API", description = "Kakao OAuth를 사용하여 로그인하고 JWT 토큰을 반환합니다.")
    public ResponseEntity<JwtResponse> loginWithKakao(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(new JwtResponse(authService.loginWithOAuth("kakao", request.getCode())));
    }

//    /**
//     * GitHub 로그인 API
//     */
//    @PostMapping("/github")
//    @Operation(summary = "GitHub 로그인 API", description = "GitHub OAuth를 사용하여 로그인하고 JWT 토큰을 반환합니다.")
//    public ResponseEntity<JwtResponse> loginWithGithub(@RequestBody OAuthLoginRequest request) {
//        return handleOAuthLogin("github", request);
//    }
//
//    /**
//     * Google 로그인 API
//     */
//    @PostMapping("/google")
//    @Operation(summary = "Google 로그인 API", description = "Google OAuth를 사용하여 로그인하고 JWT 토큰을 반환합니다.")
//    public ResponseEntity<JwtResponse> loginWithGoogle(@RequestBody OAuthLoginRequest request) {
//        return handleOAuthLogin("google", request);
//    }
//
//    /**
//     * Kakao 로그인 API
//     */
//    @PostMapping("/kakao")
//    @Operation(summary = "Kakao 로그인 API", description = "Kakao OAuth를 사용하여 로그인하고 JWT 토큰을 반환합니다.")
//    public ResponseEntity<JwtResponse> loginWithKakao(@RequestBody OAuthLoginRequest request) {
//        return handleOAuthLogin("kakao", request);
//    }
//
//    /**
//     * 공통 OAuth 로그인 처리 (GitHub, Google, Kakao)
//     */
//    private ResponseEntity<JwtResponse> handleOAuthLogin(String provider, OAuthLoginRequest request) {
//        if (request.getCode() == null || request.getCode().isEmpty()) {
//            logger.error("❌ {} OAuth 로그인 실패: 인증 코드가 없음!", provider);
//            throw new RuntimeException(provider + " OAuth 인증 코드가 없습니다.");
//        }
//        String jwt = authService.loginWithGithub(provider, request.getCode());
//        return ResponseEntity.ok(new JwtResponse(jwt));
//    }

    /**
     * 로그아웃 API
     * - 블랙리스트에 토큰 추가
     * - 세션 무효화
     * - 쿠키 삭제
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "현재 사용자의 토큰을 블랙리스트에 추가하고, 세션을 무효화한 후 쿠키를 삭제합니다.")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok("로그아웃 완료 ✅");
    }

    /**
     * 회원 탈퇴 API
     * - 계정을 소프트 삭제 처리
     * - 로그아웃 로직과 동일하게 처리
     */
    @PostMapping("/withdraw")
    @Operation(summary = "회원 탈퇴 API", description = "현재 사용자의 계정을 소프트 삭제 처리합니다. 로그아웃 로직과 동일한 절차를 따릅니다.")
    public ResponseEntity<String> withdraw(HttpServletRequest request, HttpServletResponse response) {
        return authService.withdraw(request, response);
    }

    /**
     * 계정 복구 API
     * - 탈퇴한 계정을 다시 활성화
     */
    @PostMapping("/restore")
    @Operation(summary = "회원 복구 API", description = "탈퇴한 사용자의 계정을 복구합니다.")
    public ResponseEntity<String> restoreAccount(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        String email = authentication.getName();
        if (!userService.canReRegister(email)) {
            return ResponseEntity.badRequest().body("계정 복구가 불가능합니다.");
        }

        userService.restoreUser(email);
        return ResponseEntity.ok("계정 복구 완료 ✅");
    }

    /**
     * 사용자의 마지막 로그인 제공자 조회
     */
    @GetMapping("/last-login-provider")
    @Operation(summary = "최근 로그인 제공자 조회 API", description = "사용자의 마지막 로그인 제공자를 반환합니다.")
    public ResponseEntity<String> getLastLoginProvider(Authentication authentication) {
        String email = authentication.getName();

        // 이메일을 통해 ClientEntity 조회
        ClientEntity clientEntity = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return loginHistoryService.getLastLoginProvider(clientEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok("unknown"));
    }

    /**
     * 토큰 상태 확인 API
     * - 토큰이 블랙리스트에 있는지 확인
     */
    @GetMapping("/token-status")
    @Operation(summary = "토큰 블랙리스트 확인 API", description = "JWT 토큰이 블랙리스트에 있는지 확인하여 반환합니다.")
    public ResponseEntity<Map<String, Boolean>> checkTokenStatus(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        boolean isBlacklisted = authHeader != null && authService.isTokenBlacklisted(authHeader);
        return ResponseEntity.ok(Map.of("blacklisted", isBlacklisted));
    }
}