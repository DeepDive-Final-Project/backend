package com.goorm.team9.icontact.domain.sociallogin.security.exception;

public class OAuthTokenExpiredException extends RuntimeException {
    public OAuthTokenExpiredException(String message) {
        super(message);
    }
}
