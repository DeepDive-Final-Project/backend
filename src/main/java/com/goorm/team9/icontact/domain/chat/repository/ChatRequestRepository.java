package com.goorm.team9.icontact.domain.chat.repository;

import com.goorm.team9.icontact.domain.chat.entity.ChatRequest;
import com.goorm.team9.icontact.domain.chat.entity.RequestStatus;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRequestRepository extends JpaRepository<ChatRequest, Long> {
    Optional<ChatRequest> findByIdAndStatus(Long requestId, RequestStatus status);

    @Query("SELECT cr FROM ChatRequest cr WHERE " +
            "cr.sender.nickName = :sender AND cr.receiver.nickName = :receiver " +
            "AND cr.status = 'PENDING'")
    Optional<ChatRequest> findPendingRequest(@Param("sender") String sender, @Param("receiver") String receiver);

    @Query("SELECT cr FROM ChatRequest cr WHERE cr.receiver.nickName = :receiver AND cr.status = :status")
    List<ChatRequest> findByReceiverNicknameAndStatus(@Param("receiver") String receiver, @Param("status") RequestStatus status);

    @Query("SELECT cr FROM ChatRequest cr WHERE cr.sender.nickName = :sender AND cr.status = :status")
    List<ChatRequest> findBySenderNicknameAndStatus(@Param("sender") String sender, @Param("status") RequestStatus status);

    @Query("SELECT COUNT(cr) " +
            "FROM ChatRequest cr " +
            "WHERE cr.receiverNickname = :receiver AND cr.status = :status")
    long countReceivedRequests(@Param("receiver") String receiver, @Param("status") RequestStatus status);

    @Query("SELECT COUNT(cr) " +
            "FROM ChatRequest cr " +
            "WHERE cr.senderNickname = :sender AND cr.status = :status")
    long countSentRequests(@Param("sender") String sender, @Param("status") RequestStatus status);

    List<ChatRequest> status(RequestStatus status);

}
