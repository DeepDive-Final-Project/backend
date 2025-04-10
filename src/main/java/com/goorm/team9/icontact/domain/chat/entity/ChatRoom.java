package com.goorm.team9.icontact.domain.chat.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @JoinColumn(name = "sender_nickname", referencedColumnName = "nickName", nullable = false)
    private ClientEntity senderNickname;

    @ManyToOne
    @JoinColumn(name = "receiver_nickname", referencedColumnName = "nickName", nullable = false)
    private ClientEntity receiverNickname;

    @Column(name = "last_message", length = 1000)
    private String lastMessage;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "chat_room_exit_status", joinColumns = @JoinColumn(name = "room_id"))
    @MapKeyColumn(name = "nickname")
    @Column(name = "exit_status")
    private Map<String, Boolean> exitStatus = new HashMap<>();

    public static ChatRoom createChatRoom(ClientEntity sender, ClientEntity receiver) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSenderNickname(sender);
        chatRoom.setReceiverNickname(receiver);
        chatRoom.setLastMessage(null);
        chatRoom.setLastMessageTime(null);
        return chatRoom;
    }

    public void updateLastMessage(String lastMessage, LocalDateTime lastMessageTime) {
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

}