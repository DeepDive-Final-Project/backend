package com.goorm.team9.icontact.domain.sociallogin.security.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Component("socialLoginGlobalExceptionHandler")
public class GlobalExceptionHandler {

    @ExceptionHandler(OAuthTokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleOAuthTokenExpiredException(OAuthTokenExpiredException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "OAuthTokenExpired");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}

