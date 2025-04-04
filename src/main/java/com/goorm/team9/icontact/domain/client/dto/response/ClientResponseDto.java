package com.goorm.team9.icontact.domain.client.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ClientResponseDto {

    // client
    private Long id;
    private String nickName;
    private String email;
    private String role;
    private String career;
    private String introduction;
    private List<ClientLinkResponseDto> links;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // it_topic
    private String topic1;
    private String topic2;
    private String topic3;

}
