package com.goorm.team9.icontact.chat.repository;

import com.goorm.team9.icontact.chat.entity.ChatMessage;
import com.goorm.team9.icontact.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom);
}
