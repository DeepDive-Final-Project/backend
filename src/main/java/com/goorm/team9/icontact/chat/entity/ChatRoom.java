package com.goorm.team9.icontact.chat.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "sender_nickname", referencedColumnName ="nickname", nullable = false)
    private ClientEntity senderNickname;

    @ManyToOne
    @JoinColumn(name = "receiver_nickname", referencedColumnName = "nickname", nullable = false)
    private ClientEntity receiverNickname;

    @Column(name = "last_message", length = 200)
    private String lastMessage;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @ElementCollection
    @CollectionTable(name = "chat_room_exit_status", joinColumns = @JoinColumn(name = "room_id"))
    @MapKeyColumn(name = "nickname")
    @Column(name = "exit_status")
    private Map<String, Boolean> exitStatus = new HashMap<>();

    public static ChatRoom createChatRoom(ClientEntity senderNickname, ClientEntity receiverNickname) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSenderNickname(senderNickname);
        chatRoom.setReceiverNickname(receiverNickname);
        chatRoom.setLastMessage(null);
        chatRoom.setLastMessageTime(null);
        return chatRoom;
    }
}
