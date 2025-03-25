package com.goorm.team9.icontact.domain.client.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientProfileImageDTO {
    private Long clientId;
    private String profileImageUrl;
}
