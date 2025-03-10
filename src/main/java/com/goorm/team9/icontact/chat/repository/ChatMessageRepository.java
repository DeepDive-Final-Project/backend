package com.goorm.team9.icontact.chat.repository;

import com.goorm.team9.icontact.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
