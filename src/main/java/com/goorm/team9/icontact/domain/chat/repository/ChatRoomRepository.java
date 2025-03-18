package com.goorm.team9.icontact.domain.chat.repository;

import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c " +
            "FROM ChatRoom c" +
            " WHERE c.senderNickname.nickName = :nickname OR c.receiverNickname.nickName = :nickname")
    List<ChatRoom> findBySenderNicknameOrReceiverNickname(@Param("nickname") String nickname);

    @Query("SELECT c FROM ChatRoom c WHERE " +
            "(c.senderNickname.nickName = :sender AND c.receiverNickname.nickName = :receiver) " +
            "OR (c.senderNickname.nickName = :receiver AND c.receiverNickname.nickName = :sender)")
    Optional<ChatRoom> findExistingChatRoom(@Param("sender") String sender, @Param("receiver") String receiver);
}