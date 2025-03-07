package com.goorm.team9.icontact.chat.dto;

import com.goorm.team9.icontact.chat.entity.ChatMessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageDto {

    private Long chatRoomId;
    private String senderNickname;
    private String content;

    private ChatMessageType type;

    public static ChatMessageDto createChatMessage(Long chatRoomId, String senderNickname, String content) {
        ChatMessageDto message = new ChatMessageDto();
        message.chatRoomId = chatRoomId;
        message.senderNickname = senderNickname;
        message.content = content;
        message.type = ChatMessageType.CHAT;
        return message;
    }

    public static ChatMessageDto createJoinMessage(Long chatRoomId, String senderNickname) {
        ChatMessageDto message = new ChatMessageDto();
        message.chatRoomId = chatRoomId;
        message.senderNickname = senderNickname;
        message.content = senderNickname + "님이 입장했습니다.";
        message.type = ChatMessageType.JOIN;
        return message;
    }

    public static ChatMessageDto createLeaveMessage(Long chatRoomId, String senderNickname) {
        ChatMessageDto message = new ChatMessageDto();
        message.chatRoomId = chatRoomId;
        message.senderNickname = senderNickname;
        message.content = senderNickname + "님이 퇴장했습니다.";
        message.type = ChatMessageType.LEAVE;
        return message;
    }
}
