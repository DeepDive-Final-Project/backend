package com.goorm.team9.icontact.domain.client.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientLinkRequestDTO {
    private String title;
    private String link;
}
