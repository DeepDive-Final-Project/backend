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
    @JoinColumn(name = "sender_id", nullable = false)
    private ClientEntity sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private ClientEntity receiver;

    @Column(name = "sender_nickname", nullable = false)
    private String senderNickname;

    @Column(name = "receiver_nickname", nullable = false)
    private String receiverNickname;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    public static ChatRequest create(ClientEntity senderNickname, ClientEntity receiverNickname) {
        return new ChatRequest(senderNickname, receiverNickname);
    }

    public ChatRequest(ClientEntity sender, ClientEntity receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderNickname = sender.getNickName();
        this.receiverNickname = receiver.getNickName();
        this.status = RequestStatus.PENDING;
    }

    public void accept() {
        this.status = RequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = RequestStatus.REJECTED;
    }
}