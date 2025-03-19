package com.goorm.team9.icontact.domain.chat.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "sender_nickname", nullable = false)
    private ClientEntity senderNickname;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    private ChatMessageType type;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static ChatMessage createChatMessage(ChatRoom chatRoom, ClientEntity senderNickname, String content, ChatMessageType type) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderNickname(senderNickname)
                .content(content)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void markRead() {
        this.isRead = true;
    }
}
