package com.goorm.team9.icontact.domain.chat.entity;

import com.goorm.team9.icontact.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "sender_nickname", nullable = false)
    private String senderNickname;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    private ChatMessageType type;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    public static ChatMessage createChatMessage(ChatRoom chatRoom, String senderNickname, String content, ChatMessageType type) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderNickname(senderNickname)
                .content(content)
                .type(type)
                .isRead(false)
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
