package com.goorm.team9.icontact.domain.chat.repository;

import com.goorm.team9.icontact.domain.chat.entity.ChatJoin;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatJoinRepository extends JpaRepository<ChatJoin, Long> {
    Optional<ChatJoin> findByChatRoomAndClientId(ChatRoom chatRoom, Long clientId);

    @Query("SELECT COUNT(cj) " +
            "FROM ChatJoin cj " +
            "WHERE cj.chatRoom = :chatRoom AND cj.exited = false")
    long countByChatRoomAndExitedFalse(@Param("chatRoom") ChatRoom chatRoom);

    @Query("SELECT cj FROM ChatJoin cj WHERE cj.chatRoom = :chatRoom")
    List<ChatJoin> findByChatRoom(@Param("chatRoom") ChatRoom chatRoom);

    @Query("SELECT COUNT(cj) > 0 " +
            "FROM ChatJoin cj " +
            "WHERE cj.chatRoom = :chatRoom AND cj.client.id = :clientId")
    boolean existsByChatRoomAndClientId(@Param("chatRoom") ChatRoom chatRoom, @Param("clientId") Long clientId);
}
