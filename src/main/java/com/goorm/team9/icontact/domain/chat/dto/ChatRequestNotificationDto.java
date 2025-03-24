package com.goorm.team9.icontact.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRequestNotificationDto {
    private String senderNickname;
    private Long requestId;
    private LocalDateTime requestedAt;
}
