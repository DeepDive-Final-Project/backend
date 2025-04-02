package com.goorm.team9.icontact.domain.sociallogin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthLoginResponseDto {
    private String email;
    private String provider;
    private String accessToken;
    private String refreshToken;
    private String role;
    private boolean isNewUser;
    private String nickname;

    public OAuthLoginResponseDto(String email, String provider, String accessToken, String refreshToken, String role, boolean isNewUser, String nickname) {
        this.email = email;
        this.provider = provider;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.isNewUser = isNewUser;
        this.nickname = nickname;
    }
}
