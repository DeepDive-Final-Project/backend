package com.goorm.team9.icontact.domain.sociallogin.dto;

public class OAuthTokenResponse {
    private final String email;
    private final long expiresAt; // Access Token 만료 시간 (밀리초)

    public OAuthTokenResponse(String email, long expiresAt) {
        this.email = email;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
}
