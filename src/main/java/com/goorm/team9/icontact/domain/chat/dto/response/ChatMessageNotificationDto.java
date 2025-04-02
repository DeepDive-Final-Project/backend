package com.goorm.team9.icontact.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageNotificationDto {

    private String senderNickname;
    private String messagePreview;
    private Long roomId;
    private LocalDateTime sentAt;

}
