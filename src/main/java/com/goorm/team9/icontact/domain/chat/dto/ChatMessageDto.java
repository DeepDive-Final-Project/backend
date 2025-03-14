package com.goorm.team9.icontact.domain.chat.dto;

import com.goorm.team9.icontact.domain.chat.entity.ChatMessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageDto {

    private Long roomId;
    private String senderNickname;
    private String content;

    private ChatMessageType type;

    public static ChatMessageDto createChatMessage(Long roomId, String senderNickname, String content) {
        ChatMessageDto message = new ChatMessageDto();
        message.roomId = roomId;
        message.senderNickname = senderNickname;
        message.content = content;
        message.type = ChatMessageType.CHAT;
        return message;
    }

    public static ChatMessageDto createJoinMessage(Long roomId, String senderNickname) {
        ChatMessageDto message = new ChatMessageDto();
        message.roomId = roomId;
        message.senderNickname = senderNickname;
        message.content = senderNickname + "님과의 대화가 시작되었어요.";
        message.type = ChatMessageType.JOIN;
        return message;
    }

    public static ChatMessageDto createLeaveMessage(Long roomId, String senderNickname) {
        ChatMessageDto message = new ChatMessageDto();
        message.roomId = roomId;
        message.senderNickname = senderNickname;
        message.content = senderNickname + "님이 퇴장했습니다.";
        message.type = ChatMessageType.LEAVE;
        return message;
    }
}
