package com.goorm.team9.icontact.domain.chat.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.*;
import lombok.*;


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

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private ChatMessageType type;

    public static ChatMessage createChatMessage(ChatRoom chatRoom, ClientEntity senderNickname, String content, ChatMessageType type) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderNickname(senderNickname)
                .content(content)
                .type(type)
                .build();
    }
}
