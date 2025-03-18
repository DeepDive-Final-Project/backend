package com.goorm.team9.icontact.domain.client.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ClientResponseDTO {
    // client
    private Long id;
    private String nickName;
    private String email;
    private String role;
    private String career;
    private String status;
    private String introduction;
    private String link;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // it_topic
    private String topic1;
    private String topic2;
    private String topic3;
    private String language;
    private String framework;
}
