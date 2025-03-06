package com.goorm.team9.icontact.sociallogin.controller;

import com.goorm.team9.icontact.sociallogin.security.jwt.JwtTokenProvider;
import com.goorm.team9.icontact.sociallogin.service.GithubLoginStrategy;
import com.goorm.team9.icontact.sociallogin.service.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("Authorization", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // 클라이언트에서 Authorization 헤더 삭제하도록 요청
        response.setHeader("Authorization", "");

        return ResponseEntity.ok("Logged out");
    }
}
