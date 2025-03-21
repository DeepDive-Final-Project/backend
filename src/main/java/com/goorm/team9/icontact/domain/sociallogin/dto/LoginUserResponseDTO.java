package com.goorm.team9.icontact.domain.sociallogin.dto;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.sociallogin.entity.OAuth;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserResponseDTO {
    private Long id;
    private String email;
    private String nickName;
    private String role;
    private String provider;
    private LocalDateTime lastLoginAt;

    public LoginUserResponseDTO(ClientEntity client,String provider, LocalDateTime lastLoginAt) {
        this.id = client.getId();
        this.email = client.getEmail();
        this.nickName = client.getNickName();
        this.role = client.getRole().toString();

        // OAuth 데이터에서 provider 가져오기
        List<OAuth> oauthAccounts = client.getOauthAccounts();
        if (oauthAccounts == null || oauthAccounts.isEmpty()) {
            this.provider = "UNKNOWN";
        } else {
            this.provider = oauthAccounts.get(0).getProvider();
        }

        this.lastLoginAt = lastLoginAt;
    }
}
