package com.goorm.team9.icontact.domain.sociallogin.controller;

import com.goorm.team9.icontact.domain.sociallogin.dto.JwtResponse;
import com.goorm.team9.icontact.domain.sociallogin.dto.OAuthLoginRequest;
import com.goorm.team9.icontact.domain.sociallogin.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * 서버 상태 확인용 엔드포인트
     */
    @GetMapping("/home")
    public String home() {
        return "Hello, Home!";
    }

    /**
     * GitHub 로그인 API
     * @param request GitHub 인증 코드 요청 객체
     * @return JWT 토큰 반환
     */
    @PostMapping("/github")
    public ResponseEntity<JwtResponse> loginWithGithub(@RequestBody OAuthLoginRequest request) {
        logger.info("🔄 GitHub OAuth 로그인 요청: 받은 코드={}", request.getCode());
        if (request.getCode() == null || request.getCode().isEmpty()) {
            logger.error("❌ GitHub OAuth 로그인 실패: 받은 코드가 없음!");
            throw new RuntimeException("GitHub OAuth 인증 코드가 없습니다.");
        }
        String jwt = authService.loginWithGithub(request.getCode());
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    /**
     * 로그아웃 API
     * - 블랙리스트에 토큰 추가
     * - 세션 무효화
     * - 쿠키 삭제
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok("로그아웃 완료 ✅");
    }

    /**
     * 토큰 상태 확인 API
     * - 토큰이 블랙리스트에 있는지 확인
     */
    @GetMapping("/token-status")
    public ResponseEntity<Map<String, Boolean>> checkTokenStatus(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        boolean isBlacklisted = authHeader != null && authService.isTokenBlacklisted(authHeader);
        return ResponseEntity.ok(Map.of("blacklisted", isBlacklisted));
    }

    /**
     * 회원 탈퇴 API
     * - 계정을 소프트 삭제 처리
     * - 로그아웃 로직과 동일하게 처리
     */
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(HttpServletRequest request, HttpServletResponse response) {
        return authService.withdraw(request, response);
    }
}