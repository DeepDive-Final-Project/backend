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

    private static final int MAX_CHAT_ROOMS = 5;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatJoinRepository chatJoinRepository, ClientRepository clientRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatJoinRepository = chatJoinRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public Long createOrGetRoomId(ClientEntity sender, ClientEntity receiver) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderNicknameOrReceiverNickname(sender.getNickName());

        for (ChatRoom chatRoom : chatRooms) {
            if ((chatRoom.getSenderNickname().equals(sender) && chatRoom.getReceiverNickname().equals(receiver)) ||
                    (chatRoom.getSenderNickname().equals(receiver) && chatRoom.getReceiverNickname().equals(sender))) {
                return chatRoom.getRoomId();
            }
        }

        return createChatRoom(sender, receiver);
    }

    @Transactional
    public Long createChatRoom(ClientEntity sender, ClientEntity receiver) {
        int senderChatCount = chatRoomRepository.countBySenderNicknameOrReceiverNickname(sender);
        int receiverChatCount = chatRoomRepository.countBySenderNicknameOrReceiverNickname(receiver);

        if (senderChatCount >= MAX_CHAT_ROOMS) {
            throw new IllegalArgumentException("사용자는 최대 " + MAX_CHAT_ROOMS + "개의 채팅방만 가질 수 있습니다.");
        }

        if (receiverChatCount >= MAX_CHAT_ROOMS) {
            throw new IllegalArgumentException("대상 사용자는 최대 " + MAX_CHAT_ROOMS + "개의 채팅방만 가질 수 있습니다.");
        }

        ChatRoom chatRoom = ChatRoom.createChatRoom(sender, receiver);
        chatRoomRepository.save(chatRoom);

        ChatJoin senderJoin = new ChatJoin();
        senderJoin.setChatRoom(chatRoom);
        senderJoin.setClient(sender);
        senderJoin.setExited(false);
        chatJoinRepository.save(senderJoin);

        ChatJoin receiverJoin = new ChatJoin();
        receiverJoin.setChatRoom(chatRoom);
        receiverJoin.setClient(receiver);
        receiverJoin.setExited(false);
        chatJoinRepository.save(receiverJoin);

        return chatRoom.getRoomId();
    }

    @Transactional
    public void exitChatRoom(Long roomId, Long clientId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatJoin chatJoin = chatJoinRepository.findByChatRoomAndClientId(chatRoom, clientId)
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
                    Optional<ChatJoin> chatJoin = chatJoinRepository.findByChatRoomAndClientId(chatRoom, client.getId());
                    return chatJoin.isEmpty() || !chatJoin.get().isExited();
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
