package com.goorm.team9.icontact.chat.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ChatRoomDto {

    @Getter
    @Setter
    public static class ChatRoomResponse {
        private Long roomId;
        private String senderNickname;
        private String receiverNickname;

        public ChatRoomResponse(Long roomId, String senderNickname, String receiverNickname) {
            this.roomId = roomId;
            this.senderNickname = senderNickname;
            this.receiverNickname = receiverNickname;
        }
    }

    @Getter
    @Setter
    public static class ChatRoomRequest {
        private String senderNickname;
        private String receiverNickname;
    }

    private Long roomId;
    private String senderNickname;
    private String receiverNickname;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private List<String> participants;
}
