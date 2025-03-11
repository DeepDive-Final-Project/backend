package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.chat.dto.ChatRoomResponse;
import com.goorm.team9.icontact.chat.entity.ChatRoom;
import com.goorm.team9.icontact.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    @Transactional
    public Long createOrGetRoomId(String senderNickname, String receiverNickname) {
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findBySenderNicknameAndReceiverNickname(senderNickname, receiverNickname);

        if (existingChatRoom.isPresent()) {
            return existingChatRoom.get().getRoomId();
        } else {
            return createChatRoom(senderNickname, receiverNickname);
        }
    }

    @Transactional
    public Long createChatRoom(String senderNickname, String receiverNickname) {
        ChatRoom chatRoom = ChatRoom.createChatRoom(senderNickname, receiverNickname);
        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

    public List<ChatRoomResponse> getChatRoomsByUser(String nickname) {
        return chatRoomRepository.findBySenderNicknameOrReceiverNickname(nickname, nickname)
                .stream()
                .map(ChatRoomResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ChatRoomResponse> getAllChatRooms() {
        return chatRoomRepository.findAll()
                .stream()
                .map(ChatRoomResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
