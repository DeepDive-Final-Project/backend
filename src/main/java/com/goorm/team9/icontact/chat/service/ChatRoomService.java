package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.chat.entity.ChatRoom;
import com.goorm.team9.icontact.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    public Long createOrGetChatRoomId(String senderNickname, String receiverNickname) {
        ChatRoom existingChatRoom = chatRoomRepository.findByParticipants(senderNickname, receiverNickname);

        if (existingChatRoom != null) {
            return existingChatRoom.getChatRoomId();
        } else {
            return createChatRoom(senderNickname, receiverNickname);
        }
    }

    @Transactional
    public Long createChatRoom(String senderNickname, String receiverNickname) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSenderNickname(senderNickname);
        chatRoom.setReceiverNickname(receiverNickname);
        chatRoomRepository.save(chatRoom);
        return chatRoom.getChatRoomId();
    }

    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElse(null);
    }

    public List<ChatRoom> getChatRoomsByUser(String nickname) {
        return chatRoomRepository.findBySenderNicknameOrReceiverNickname(nickname, nickname);
    }

    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }
}
