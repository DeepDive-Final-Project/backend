package com.goorm.team9.icontact.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "sender_nickname", length = 100)
    private String senderNickname;

    @Column(name = "receiver_nickname", length = 100)
    private String receiverNickname;

    @Column(name = "last_message", length = 200)
    private String lastMessage;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    public static ChatRoom createChatRoom(String senderNickname, String receiverNickname) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSenderNickname(senderNickname);
        chatRoom.setReceiverNickname(receiverNickname);
        chatRoom.setLastMessage(null);
        chatRoom.setLastMessageTime(null);
        return chatRoom;
    }
}
