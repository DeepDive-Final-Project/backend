package com.goorm.team9.icontact.domain.chat.repository;

import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c " +
            "FROM ChatRoom c" +
            " WHERE c.senderNickname.nickName = :nickname OR c.receiverNickname.nickName = :nickname")
    List<ChatRoom> findBySenderNicknameOrReceiverNickname(@Param("nickname") String nickname);

    @Query("SELECT COUNT(DISTINCT c) " +
            "FROM ChatRoom c " +
            "JOIN ChatJoin j ON c = j.chatRoom " +
            "WHERE j.client = :client AND j.exited = false")
    int countActiveChatRoomsByClient(@Param("client") ClientEntity client);
}