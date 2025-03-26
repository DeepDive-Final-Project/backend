package com.goorm.team9.icontact.domain.client.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClientProfileImageDTO {
    private Long clientId;
    private String profileImageUrl;
}
