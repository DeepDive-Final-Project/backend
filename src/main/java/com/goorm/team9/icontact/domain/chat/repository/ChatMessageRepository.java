package com.goorm.team9.icontact.domain.chat.repository;

import com.goorm.team9.icontact.domain.chat.entity.ChatMessage;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom);

    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatRoom.roomId = :chatRoomId " +
            "AND m.created_at > (SELECT cj.lastReadAt FROM ChatJoin cj WHERE cj.chatRoom.roomId = :chatRoomId AND cj.client.id = :clientId)")
    long countUnreadMessages(@Param("chatRoomId") Long chatRoomId, @Param("clientId") Long clientId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.roomId = :roomId ORDER BY m.created_at ASC")
    List<ChatMessage> findByChatRoomId(@Param("roomId") Long roomId);

    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatRoom = :chatRoom " +
            "AND m.isRead = false " +
            "AND m.senderNickname <> :reader")
    List<ChatMessage> findUnreadMessages(@Param("chatRoom") ChatRoom chatRoom,
                                         @Param("reader") ClientEntity reader);
}