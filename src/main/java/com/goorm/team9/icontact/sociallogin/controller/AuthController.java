package com.goorm.team9.icontact.sociallogin.controller;

import com.goorm.team9.icontact.sociallogin.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuthService oAuthService;

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

        return ResponseEntity.ok(userInfo);
    }
}
