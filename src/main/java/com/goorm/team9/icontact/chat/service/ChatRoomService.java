package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.chat.dto.ChatRoomResponse;
import com.goorm.team9.icontact.chat.entity.ChatJoin;
import com.goorm.team9.icontact.chat.entity.ChatRoom;
import com.goorm.team9.icontact.chat.repository.ChatJoinRepository;
import com.goorm.team9.icontact.chat.repository.ChatRoomRepository;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatJoinRepository chatJoinRepository;
    private final ClientRepository clientRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatJoinRepository chatJoinRepository, ClientRepository clientRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatJoinRepository = chatJoinRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public Long createOrGetRoomId(ClientEntity senderNickname, ClientEntity receiverNickname) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderNicknameOrReceiverNickname(senderNickname.getNickName());

        for (ChatRoom chatRoom : chatRooms) {
            if ((chatRoom.getSenderNickname().equals(senderNickname) && chatRoom.getReceiverNickname().equals(receiverNickname)) ||
                    (chatRoom.getSenderNickname().equals(receiverNickname) && chatRoom.getReceiverNickname().equals(senderNickname))) {
                return chatRoom.getRoomId();
            }
        }

        return createChatRoom(senderNickname, receiverNickname);
    }

    @Transactional
    public Long createChatRoom(ClientEntity senderNickname, ClientEntity receiverNickname) {
        ChatRoom chatRoom = ChatRoom.createChatRoom(senderNickname, receiverNickname);
        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

    @Transactional
    public void exitChatRoom(Long roomId, Long clientId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatJoin chatJoin = chatJoinRepository.findByChatRoomAndClientId(chatRoom, client)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 채팅방에 존재하지 않습니다."));

        if (chatJoin.isExited()) {
            throw new IllegalArgumentException("이미 나간 채팅방입니다.");
        }

        chatJoin.exitChatRoom();
        chatJoinRepository.save(chatJoin);
    }

    public List<ChatRoomResponse> getChatRoomsByUser(ClientEntity client) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderNicknameOrReceiverNickname(client.getNickName());

        return chatRooms.stream()
                .filter(chatRoom -> {
                    ChatJoin chatJoin = chatJoinRepository.findByChatRoomAndClientId(chatRoom, client)
                            .orElse(null);
                    return chatJoin == null || !chatJoin.isExited();
                })
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
