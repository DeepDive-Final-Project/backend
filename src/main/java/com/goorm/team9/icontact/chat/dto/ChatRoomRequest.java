package com.goorm.team9.icontact.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomRequest {
    private String senderNickname;
    private String receiverNickname;
}
