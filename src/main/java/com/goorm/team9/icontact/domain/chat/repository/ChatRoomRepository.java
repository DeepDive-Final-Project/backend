package com.goorm.team9.icontact.domain.chat.repository;

import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c FROM ChatRoom c " +
            "WHERE c.sender.nickName = :nickname OR c.receiver.nickName = :nickname")
    List<ChatRoom> findBySenderNicknameOrReceiverNickname(@Param("nickname") String nickname);

    @Query("SELECT c FROM ChatRoom c WHERE " +
            "(c.sender.nickName = :sender AND c.receiver.nickName = :receiver) " +
            "OR (c.sender.nickName = :receiver AND c.receiver.nickName = :sender)")
    Optional<ChatRoom> findExistingChatRoom(@Param("sender") String sender, @Param("receiver") String receiver);

    @Query("SELECT c, " +
            "(SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatRoom = c AND m.created_at > " +
            "(SELECT cj.lastReadAt FROM ChatJoin cj WHERE cj.chatRoom = c AND cj.client.id = :clientId)) " +
            "FROM ChatRoom c " +
            "WHERE c.sender.nickName = :nickname OR c.receiver.nickName = :nickname " +
            "ORDER BY (SELECT MAX(m.created_at) FROM ChatMessage m WHERE m.chatRoom = c) DESC")
    List<Object[]> findUnreadChatRoomsWithUnreadCount(@Param("nickname") String nickname, @Param("clientId") Long clientId);

    @Query("SELECT c, " +
            "(SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatRoom = c AND m.created_at > " +
            "(SELECT cj.lastReadAt FROM ChatJoin cj WHERE cj.chatRoom = c AND cj.client.id = :clientId)) " +
            "FROM ChatRoom c " +
            "WHERE c.sender.nickName = :nickname OR c.receiver.nickName = :nickname " +
            "ORDER BY (SELECT MAX(m.created_at) FROM ChatMessage m WHERE m.chatRoom = c) DESC")
    List<Object[]> findAllChatRoomsWithUnreadCount(@Param("nickname") String nickname, @Param("clientId") Long clientId);

    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.sender.nickName = :sender AND cr.receiver.nickName = :receiver) " +
            "OR (cr.sender.nickName = :receiver AND cr.receiver.nickName = :sender)")
    Optional<ChatRoom> findBySenderAndReceiver(@Param("sender") String sender, @Param("receiver") String receiver);
}
