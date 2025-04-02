package com.goorm.team9.icontact.domain.sociallogin.controller;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.sociallogin.dto.LoginUserResponseDto;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthLoginRequestDto;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthLoginResponseDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<OAuthLoginResponseDto> loginWithGithub(@RequestBody OAuthLoginRequestDto request, HttpServletRequest httpRequest) {
        OAuthLoginResponseDto response = authService.loginWithOAuth("github", request.getCode(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    @Operation(summary = "Google 로그인 API", description = "Google OAuth를 사용하여 로그인하고 사용자 정보를 반환합니다.")
    public ResponseEntity<OAuthLoginResponseDto> loginWithGoogle(@RequestBody OAuthLoginRequestDto request, HttpServletRequest httpRequest) {
        OAuthLoginResponseDto response = authService.loginWithOAuth("google", request.getCode(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kakao")
    @Operation(summary = "Kakao 로그인 API", description = "Kakao OAuth를 사용하여 로그인하고 사용자 정보를 반환합니다.")
    public ResponseEntity<OAuthLoginResponseDto> loginWithKakao(@RequestBody OAuthLoginRequestDto request, HttpServletRequest httpRequest) {
        OAuthLoginResponseDto response = authService.loginWithOAuth("kakao", request.getCode(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "현재 사용자의 토큰을 블랙리스트에 추가하고, 세션을 무효화한 후 쿠키를 삭제합니다.")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok("로그아웃 완료 ✅");
    }

    @PostMapping("/withdraw")
    @Operation(summary = "회원 탈퇴 API", description = "현재 사용자의 계정을 소프트 삭제 처리합니다. 로그아웃 로직과 동일한 절차를 따릅니다.")
    public ResponseEntity<Map<String, String>> withdraw(HttpServletRequest request, HttpServletResponse response) {
        authService.withdraw(request, response);

        Map<String, String> result = Map.of(
                "status", "withdrawn",
                "message", "회원 탈퇴가 완료되었습니다."
        );
        return ResponseEntity.ok(result);
    }

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

        String token = authHeader.substring(7);
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

    @GetMapping("/last-login-provider")
    @Operation(summary = "최근 로그인 제공자 조회 API", description = "사용자의 마지막 로그인 제공자를 반환합니다.")
    public ResponseEntity<String> getLastLoginProvider(Authentication authentication) {
        String email = authentication.getName();

        ClientEntity clientEntity = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return loginHistoryService.getLastLoginProvider(clientEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok("unknown"));
    }

    @GetMapping("/token-status")
    @Operation(summary = "토큰 블랙리스트 확인 API", description = "JWT 토큰이 블랙리스트에 있는지 확인하여 반환합니다.")
    public ResponseEntity<Map<String, Boolean>> checkTokenStatus(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        boolean isBlacklisted = authHeader != null && authService.isTokenBlacklisted(authHeader);
        return ResponseEntity.ok(Map.of("blacklisted", isBlacklisted));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회 API")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyInfo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                       HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT 토큰이 없습니다.");
        }

        try {
            String email = jwtTokenProvider.getUserEmail(token);
            String provider = jwtTokenProvider.getProvider(token);

            Optional<OAuth> oauthInfo = oAuthRepository.findByProviderAndEmail(email, provider);

            return clientRepository.findByEmailAndProviderAndIsDeletedFalse(email, provider)
                    .map(client -> {
                        Optional<LoginHistory> lastLogin = loginHistoryRepository.findFirstByClientEntityOrderByLoginAtDesc(client);
                        LocalDateTime lastLoginAt = lastLogin.map(LoginHistory::getLoginAt).orElse(null);

                        return ResponseEntity.ok(new LoginUserResponseDto(client, provider, lastLoginAt));
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
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