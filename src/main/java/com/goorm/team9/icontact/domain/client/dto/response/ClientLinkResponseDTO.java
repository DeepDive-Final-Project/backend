package com.goorm.team9.icontact.domain.client.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientLinkResponseDTO {
    private String title;
    private String link;
}
