package com.goorm.team9.icontact.domain.sociallogin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthLoginRequestDto {
    private String code;

    public OAuthLoginRequestDto(String code) {
        this.code = code;
    }
}

