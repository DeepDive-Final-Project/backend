package com.goorm.team9.icontact.domain.chat.dto;

import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ChatRoomResponse {
    private Long roomId;
    private List<String> participants;
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    public static ChatRoomResponse fromEntity(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getRoomId(),
                List.of(chatRoom.getSenderNickname().getNickName(), chatRoom.getReceiverNickname().getNickName()),
                chatRoom.getLastMessage(),
                chatRoom.getLastMessageTime()
        );
    }
}
