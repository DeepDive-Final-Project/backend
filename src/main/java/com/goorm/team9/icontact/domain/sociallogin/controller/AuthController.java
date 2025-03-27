package com.goorm.team9.icontact.domain.sociallogin.controller;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.sociallogin.dto.JwtResponse;
import com.goorm.team9.icontact.domain.sociallogin.dto.LoginUserResponseDTO;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthLoginRequest;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthLoginResponseDTO;
import com.goorm.team9.icontact.domain.sociallogin.entity.LoginHistory;
import com.goorm.team9.icontact.domain.sociallogin.entity.OAuth;
import com.goorm.team9.icontact.domain.sociallogin.repository.LoginHistoryRepository;
import com.goorm.team9.icontact.domain.sociallogin.repository.OAuthRepository;
import com.goorm.team9.icontact.domain.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.domain.sociallogin.service.AuthService;
import com.goorm.team9.icontact.domain.sociallogin.service.LoginHistoryService;
import com.goorm.team9.icontact.domain.sociallogin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthRepository oAuthRepository;
    private final LoginHistoryRepository loginHistoryRepository;
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
    @Operation(summary = "GitHub 로그인 API", description = "GitHub OAuth를 사용하여 로그인하고 사용자 정보를 반환합니다.")
    public ResponseEntity<OAuthLoginResponseDTO> loginWithGithub(@RequestBody OAuthLoginRequest request, HttpServletRequest httpRequest) {
        OAuthLoginResponseDTO response = authService.loginWithOAuth("github", request.getCode(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    @Operation(summary = "Google 로그인 API", description = "Google OAuth를 사용하여 로그인하고 사용자 정보를 반환합니다.")
    public ResponseEntity<OAuthLoginResponseDTO> loginWithGoogle(@RequestBody OAuthLoginRequest request, HttpServletRequest httpRequest) {
        OAuthLoginResponseDTO response = authService.loginWithOAuth("google", request.getCode(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kakao")
    @Operation(summary = "Kakao 로그인 API", description = "Kakao OAuth를 사용하여 로그인하고 사용자 정보를 반환합니다.")
    public ResponseEntity<OAuthLoginResponseDTO> loginWithKakao(@RequestBody OAuthLoginRequest request, HttpServletRequest httpRequest) {
        OAuthLoginResponseDTO response = authService.loginWithOAuth("kakao", request.getCode(), httpRequest);
        return ResponseEntity.ok(response);
    }

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
    public ResponseEntity<Map<String, String>> withdraw(HttpServletRequest request, HttpServletResponse response) {
        authService.withdraw(request, response); // 실제 탈퇴 처리

        Map<String, String> result = Map.of(
                "status", "withdrawn",
                "message", "회원 탈퇴가 완료되었습니다."
        );
        return ResponseEntity.ok(result);
    }

    /**
     * 계정 복구 API
     * - 탈퇴한 계정을 다시 활성화
     */
    @PostMapping("/restore")
    @Operation(summary = "회원 복구 API", description = "탈퇴한 사용자의 계정을 복구합니다.")
    public ResponseEntity<Map<String, String>> restoreAccount(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, String> error = Map.of(
                    "status", "unauthorized",
                    "message", "인증되지 않은 사용자입니다."
            );
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(error);
        }

        String token = authHeader.substring(7); // "Bearer " 제거
        if (authService.isTokenBlacklisted(authHeader)) {
            Map<String, String> error = Map.of(
                    "status", "blacklisted",
                    "message", "해당 토큰은 블랙리스트에 등록되어 있습니다."
            );
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(error);
        }

        String email = jwtTokenProvider.getUserEmail(token);
        String provider = jwtTokenProvider.getProvider(token);

        Optional<ClientEntity> deletedUser = clientRepository.findByEmailAndProviderAndIsDeletedTrue(email, provider);
        if (deletedUser.isEmpty()) {
            Map<String, String> error = Map.of(
                    "status", "not_found",
                    "message", "계정 복구가 불가능합니다."
            );
            return ResponseEntity.badRequest().body(error);
        }

        userService.restoreUser(email, provider);

        Map<String, String> result = Map.of(
                "status", "restored",
                "message", "계정 복구가 완료되었습니다."
        );
        return ResponseEntity.ok(result);
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

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회 API")
    @Transactional(readOnly = true) // ✅ 추가
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization 헤더가 없습니다.");
        }

        try {
            String token = authorizationHeader.substring(7);
            String email = jwtTokenProvider.getUserEmail(token);
            String provider = jwtTokenProvider.getProvider(token);

            // OAuth 정보 조회
            Optional<OAuth> oauthInfo = oAuthRepository.findByProviderAndEmail(email, provider);

            return clientRepository.findByEmailAndProviderAndIsDeletedFalse(email,provider)
                    .map(client -> {
                        // 최근 로그인 시간 조회
                        Optional<LoginHistory> lastLogin = loginHistoryRepository.findFirstByClientEntityOrderByLoginAtDesc(client);
                        LocalDateTime lastLoginAt = lastLogin.map(LoginHistory::getLoginAt).orElse(null);

                        return ResponseEntity.ok(new LoginUserResponseDTO(client, provider, lastLoginAt));
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace(); // ❗ 예외 스택 트레이스 출력 추가
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    @RequestMapping("/test")
    public ResponseEntity<String> test(HttpServletRequest request) {
        System.out.println("🔍 request.getScheme() = " + request.getScheme());
        System.out.println("🔍 request.getServerName() = " + request.getServerName());
        System.out.println("🔍 request.getRemoteAddr() = " + request.getRemoteAddr());
        return ResponseEntity.ok("헤더 확인 완료");
    }

}