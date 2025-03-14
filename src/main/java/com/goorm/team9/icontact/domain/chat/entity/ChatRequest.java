package com.goorm.team9.icontact.domain.chat.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_request")
public class ChatRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_nickname", nullable = false)
    private ClientEntity senderNickname;

    @ManyToOne
    @JoinColumn(name = "receiver_nickname", nullable = false)
    private ClientEntity receiverNickname;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    public ChatRequest(ClientEntity senderNickname, ClientEntity receiverNickname) {
        this.senderNickname = senderNickname;
        this.receiverNickname = receiverNickname;
        this.status = RequestStatus.PENDING;
    }

    public void accept() {
        this.status = RequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = RequestStatus.REJECTED;
    }
}