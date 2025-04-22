package com.goorm.team9.icontact.domain.chat.entity;

import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    public static ChatRequest create(ClientEntity sender, ClientEntity receiver) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.sender = sender;
        chatRequest.receiver = receiver;
        chatRequest.status = RequestStatus.PENDING;
        return chatRequest;
    }

    public void accept() {
        this.status = RequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = RequestStatus.REJECTED;
    }
}
