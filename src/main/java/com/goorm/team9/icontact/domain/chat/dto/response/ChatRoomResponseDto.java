package com.goorm.team9.icontact.domain.chat.dto.response;

import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ChatRoomResponseDto {

    private Long roomId;
    private List<String> participants;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
    private boolean exited;
    private Long otherId;
    private String otherUserNickname;

    public static ChatRoomResponseDto fromEntity(ChatRoom chatRoom, Long unreadCount, boolean exited, Long myId) {
        boolean isSenderMe = chatRoom.getSender().getId().equals(myId);

        Long otherId = isSenderMe
                ? chatRoom.getReceiver().getId()
                : chatRoom.getSender().getId();

        String otherUserNickname = isSenderMe
                ? chatRoom.getReceiver().getNickName()
                : chatRoom.getSender().getNickName();

        return new ChatRoomResponseDto(
                chatRoom.getRoomId(),
                List.of(chatRoom.getSender().getNickName(), chatRoom.getReceiver().getNickName()),
                chatRoom.getLastMessage() != null ? chatRoom.getLastMessage() : "새 메시지가 없습니다.",
                chatRoom.getLastMessageTime(),
                unreadCount,
                exited,
                otherId,
                otherUserNickname
        );
    }

}

