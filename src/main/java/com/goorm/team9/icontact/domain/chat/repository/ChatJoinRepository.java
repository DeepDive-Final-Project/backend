package com.goorm.team9.icontact.domain.chat.repository;

import com.goorm.team9.icontact.domain.chat.entity.ChatJoin;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatJoinRepository extends JpaRepository<ChatJoin, Long> {
    Optional<ChatJoin> findByChatRoomAndClientId(
            @Param("chatRoom") ChatRoom chatRoom,
            @Param("clientId") Long clientId);
}
