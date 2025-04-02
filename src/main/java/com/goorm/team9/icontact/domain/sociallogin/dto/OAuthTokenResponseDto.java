package com.goorm.team9.icontact.domain.sociallogin.dto;

import lombok.Getter;

@Getter
public class OAuthTokenResponseDto {
    private final String email;
    private final long expiresAt;

    public OAuthTokenResponseDto(String email, long expiresAt) {
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
