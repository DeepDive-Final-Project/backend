package com.goorm.team9.icontact.sociallogin.controller;

import com.goorm.team9.icontact.sociallogin.repository.OAuthRepository;
import com.goorm.team9.icontact.sociallogin.security.jwt.JwtBlacklist;
import com.goorm.team9.icontact.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.sociallogin.service.GithubLoginStrategy;
import com.goorm.team9.icontact.sociallogin.service.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.goorm.team9.icontact.sociallogin.dto.OAuthLoginRequest;
import com.goorm.team9.icontact.sociallogin.dto.JwtResponse;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuthService oAuthService;
    private final GithubLoginStrategy githubLoginStrategy;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklist jwtBlacklist;

    /**
     * 소셜 로그인 후 사용자 정보 반환
     */
    @GetMapping("/login-info")
    public ResponseEntity<Map<String, Object>> getLoginInfo(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return ResponseEntity.badRequest().build();
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> userInfo = oAuth2User.getAttributes();

        // CustomOAuth2UserService에서 JWT를 반환하도록 변경
        if (userInfo.get("jwtToken") == null) {
            String email = (String) userInfo.get("email");
            String jwtToken = jwtTokenProvider.createToken(email);
            userInfo.put("jwtToken", jwtToken);
        }

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/github")
    public ResponseEntity<?> loginWithGithub(@RequestBody OAuthLoginRequest request) {
        String jwt = githubLoginStrategy.authenticate(request.getCode());
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

//    /**
//     * ✅ Step 1: `code`를 받지만, 백엔드에서 바로 사용하지 않고 프론트엔드로 반환
//     */
//    @GetMapping("/oauth2/code/github")
//    public ResponseEntity<Map<String, String>> getGitHubCode(@RequestParam String code, HttpServletResponse response) {
//        Map<String, String> responseBody = new HashMap<>();
//        responseBody.put("code", code);
//
//        // ✅ 리다이렉트 (예: 프론트엔드 로그인 페이지)
//        String redirectUrl = "http://localhost:3000/auth/callback?code=" + code;
//        response.setHeader("Location", redirectUrl);
//        response.setStatus(302); // 302 Redirect
//
//        return ResponseEntity.ok(responseBody);
//    }
//
//    /**
//     * ✅ Step 2: Postman 또는 프론트에서 받은 `code`를 사용해 `access_token` 요청
//     */
//    @PostMapping("/oauth2/token/github")
//    public ResponseEntity<?> getGitHubAccessToken(@RequestBody OAuthLoginRequest request) {
//        String jwt = githubLoginStrategy.authenticate(request.getCode());
//        return ResponseEntity.ok(new JwtResponse(jwt));  // 🔹 JWT 반환
//    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 현재 인증 정보 확인 (로그 출력)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🛑 [로그아웃 시도] 현재 인증 정보: " + authentication);

        // JwtTokenProvider의 resolveToken() 사용
        String token = jwtTokenProvider.resolveToken(request);
        System.out.println("🔍 [로그아웃 요청] 추출된 JWT: " + token);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            long expirationMillis = jwtTokenProvider.getExpirationMillis(token); // JWT 만료 시간 가져오기
            System.out.println("🛑 블랙리스트 추가: " + token + " 만료 시간: " + expirationMillis);
            jwtBlacklist.addToBlacklist(token, expirationMillis);
            System.out.println("✅ [블랙리스트] 추가 완료!");

            // ✅ DB에서도 해당 사용자의 accessToken 삭제 (null로 변경)
            String email = jwtTokenProvider.getUserEmail(token);
            System.out.println("🔍 [로그아웃 요청] 해당 사용자 이메일: " + email);
            oAuthService.invalidateAccessToken(email); // `accessToken`을 `null`로 업데이트
        }

        // SecurityContext 초기화 (인증 정보 삭제)
        SecurityContextHolder.getContext().setAuthentication(null);
        // SecurityContext 초기화 (JWT 기반 로그아웃)
        SecurityContextHolder.clearContext();
        System.out.println("✅ [로그아웃 완료] SecurityContext 초기화 완료");

        // 현재 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            System.out.println("✅ [로그아웃 완료] 세션 무효화 완료");
        }

        // 클라이언트가 Authorization 헤더를 삭제하도록 응답에 포함
        response.setHeader("Authorization", "");

        // 쿠키 삭제 (JWT가 쿠키에 저장된 경우)
        Cookie cookie = new Cookie("Authorization", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);  // 보안 강화
        response.addCookie(cookie);
        System.out.println("✅ [로그아웃 완료] 쿠키 삭제 완료");

        // ✅ GitHub 로그아웃 URL 추가
        String githubLogoutUrl = "https://github.com/logout";

        // JSON 응답 반환 (HTML이 아닌 JSON 응답을 강제)
        Map<String, String> responseBody = new HashMap<>();
//        responseBody.put("message", "로그아웃 완료 ✅");
        responseBody.put("redirectUrl", githubLogoutUrl);  // GitHub 로그아웃 페이지로 이동

        return ResponseEntity.ok("로그아웃 완료");
    }

//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(HttpServletResponse response) {
//        Cookie cookie = new Cookie("Authorization", null);
//        cookie.setMaxAge(0);
//        cookie.setPath("/");
//        response.addCookie(cookie);
//
//        // 클라이언트에서 Authorization 헤더 삭제하도록 요청
//        response.setHeader("Authorization", "");
//
//        return ResponseEntity.ok("Logged out");
//    }

    @GetMapping("/token-status")
    public ResponseEntity<Map<String, Boolean>> checkTokenStatus(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("blacklisted", false));
        }
        String token = authHeader.substring(7);
        boolean isBlacklisted = jwtBlacklist.isBlacklisted(token);
        return ResponseEntity.ok(Map.of("blacklisted", isBlacklisted));
    }
}
