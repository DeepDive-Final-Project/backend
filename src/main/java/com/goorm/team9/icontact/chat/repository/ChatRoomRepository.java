package com.goorm.team9.icontact.chat.repository;

import com.goorm.team9.icontact.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findBySenderNicknameAndReceiverNickname(String senderNickname, String receiverNickname);
    List<ChatRoom> findBySenderNicknameOrReceiverNickname(String senderNickname, String receiverNickname);

    @Query("SELECT c FROM ChatRoom c WHERE " +
            "(c.senderNickname = :senderNickname AND c.receiverNickname = :receiverNickname) OR " +
            "(c.senderNickname = :receiverNickname AND c.receiverNickname = :senderNickname)")

    ChatRoom findByParticipants(@Param("senderNickname") String senderNickname,
    @Param("receiverNickname") String receiverNickname);

}
