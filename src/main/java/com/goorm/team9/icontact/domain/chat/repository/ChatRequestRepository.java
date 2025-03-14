package com.goorm.team9.icontact.domain.chat.repository;

import com.goorm.team9.icontact.domain.chat.entity.ChatRequest;
import com.goorm.team9.icontact.domain.chat.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRequestRepository extends JpaRepository<ChatRequest, Long> {
    Optional<ChatRequest> findByIdAndStatus(Long requestId, RequestStatus status);
}
