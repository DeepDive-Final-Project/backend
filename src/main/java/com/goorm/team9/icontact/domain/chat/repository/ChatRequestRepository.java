package com.goorm.team9.icontact.domain.chat.repository;

import com.goorm.team9.icontact.domain.chat.entity.ChatRequest;
import com.goorm.team9.icontact.domain.chat.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRequestRepository extends JpaRepository<ChatRequest, Long> {
    Optional<ChatRequest> findByIdAndStatus(Long requestId, RequestStatus status);

    @Query("SELECT cr FROM ChatRequest cr WHERE " +
            "cr.senderNickname.nickName = :sender AND cr.receiverNickname.nickName = :receiver " +
            "AND cr.status = 'PENDING'")
    Optional<ChatRequest> findPendingRequest(@Param("sender") String sender, @Param("receiver") String receiver);
}
