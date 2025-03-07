package com.goorm.team9.icontact.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "sender_nickname", length = 100)
    private String senderNickname;

    @Column(name = "receiver_nickname", length = 100)
    private String receiverNickname;

    @Column(name = "chatting", length = 200)
    private String chatting;

    public static ChatRoom createChatRoom(String senderNickname, String receiverNickname) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSenderNickname(senderNickname);
        chatRoom.setReceiverNickname(receiverNickname);
        return chatRoom;
    }
}
