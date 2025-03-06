package com.goorm.team9.icontact.sociallogin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/home")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("OAuth 로그인 성공! 홈 화면입니다.");
    }
}

