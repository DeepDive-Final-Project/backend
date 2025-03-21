package com.goorm.team9.icontact.domain.chat.dto;

import com.goorm.team9.icontact.domain.chat.entity.ChatMessage;
import com.goorm.team9.icontact.domain.chat.entity.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

    private Long messageId;
    private Long roomId;
    private String senderNickname;
    private String content;
    private ChatMessageType type;
    private LocalDateTime timeStamp;

    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .messageId(chatMessage.getId())
                .roomId(chatMessage.getChatRoom().getRoomId())
                .senderNickname(chatMessage.getSenderNickname().getNickName())
                .content(chatMessage.getContent())
                .type(chatMessage.getType())
                .timeStamp(chatMessage.getCreated_at())
                .build();
    }

    public static ChatMessageDto createChatMessage(Long roomId, String senderNickname, String content) {
        return ChatMessageDto.builder()
                .roomId(roomId)
                .senderNickname(senderNickname)
                .content(content)
                .type(ChatMessageType.CHAT)
                .timeStamp(LocalDateTime.now())
                .build();
    }

    public static ChatMessageDto createJoinMessage(Long roomId, String senderNickname) {
        return ChatMessageDto.builder()
                .roomId(roomId)
                .senderNickname(senderNickname)
                .content(senderNickname + "님과의 대화가 시작되었어요.")
                .type(ChatMessageType.JOIN)
                .timeStamp(LocalDateTime.now())
                .build();
    }
    public static ChatMessageDto createLeaveMessage(Long roomId, String senderNickname) {
        return ChatMessageDto.builder()
                .roomId(roomId)
                .senderNickname(senderNickname)
                .content(senderNickname + "님이 퇴장했습니다.")
                .type(ChatMessageType.LEAVE)
                .timeStamp(LocalDateTime.now())
                .build();
    }
}
