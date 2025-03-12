package com.goorm.team9.icontact.chat.repository;

import com.goorm.team9.icontact.chat.entity.ChatJoin;
import com.goorm.team9.icontact.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatJoinRepository extends JpaRepository<ChatJoin, Long> {
    Optional<ChatJoin> findByChatRoomAndClientId(ChatRoom chatRoom, Long clientId);
}
