package com.goorm.team9.icontact.domain.chat.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatJoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @Column(nullable = false)
    private boolean exited = false;

    @Column(nullable = true)
    private LocalDateTime lastReadAt;

    public void exitChatRoom() {
        this.exited = true;
    }

    public void updateLastReadAt() {
        this.lastReadAt = LocalDateTime.now();
    }

}
