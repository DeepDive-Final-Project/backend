package com.goorm.team9.icontact.sociallogin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthLoginRequest {
    private String code; // GitHub OAuth에서 받은 코드

    public OAuthLoginRequest(String code) {
        this.code = code;
    }
}

