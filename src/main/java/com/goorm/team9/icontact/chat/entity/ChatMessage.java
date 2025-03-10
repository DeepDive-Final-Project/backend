package com.goorm.team9.icontact.chat.entity;

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

    private Long chatRoomId;
    private String senderNickname;
    private String content;

    @Enumerated(EnumType.STRING)
    private ChatMessageType type;

    public static ChatMessage create(Long chatRoomId, String senderNickname, String content, ChatMessageType type) {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderNickname(senderNickname)
                .content(content)
                .type(type)
                .build();
    }
}
