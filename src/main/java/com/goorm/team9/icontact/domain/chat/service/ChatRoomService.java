package com.goorm.team9.icontact.domain.chat.service;

import com.goorm.team9.icontact.domain.chat.dto.ChatRoomResponse;
import com.goorm.team9.icontact.domain.chat.entity.ChatJoin;
import com.goorm.team9.icontact.domain.chat.entity.ChatRequest;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.chat.entity.RequestStatus;
import com.goorm.team9.icontact.domain.chat.repository.ChatJoinRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRequestRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRoomRepository;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.client.service.ClientService;
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
    private final ClientService clientService;
    private final ChatRequestRepository chatRequestRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatJoinRepository chatJoinRepository, ClientRepository clientRepository, ChatRequestRepository chatRequestRepository, ClientService clientService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatJoinRepository = chatJoinRepository;
        this.clientRepository = clientRepository;
        this.chatRequestRepository = chatRequestRepository;
        this.clientService = clientService;
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
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findExistingChatRoom(sender.getNickName(), receiver.getNickName());

        if (existingChatRoom.isPresent()) {
            throw new IllegalArgumentException("이미 채팅방이 존재합니다.");
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
    public Long requestChat(ClientEntity senderNickname, ClientEntity receiverNickname) {
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findExistingChatRoom(senderNickname.getNickName(), receiverNickname.getNickName());

        if (existingChatRoom.isPresent()) {
            throw new IllegalArgumentException("이미 채팅방이 존재합니다.");
        }

        Optional<ChatRequest> existingRequest = chatRequestRepository.findPendingRequest(senderNickname.getNickName(), receiverNickname.getNickName());

        if (existingRequest.isPresent()) {
            throw new IllegalArgumentException("이미 채팅 요청을 보냈습니다.");
        }

        ChatRequest chatRequest = new ChatRequest(senderNickname, receiverNickname);
        chatRequestRepository.save(chatRequest);
        return chatRequest.getId();
    }

    @Transactional
    public Long acceptChatRequest(Long requestId) {
        ChatRequest chatRequest = chatRequestRepository.findByIdAndStatus(requestId, RequestStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청이 없거나 이미 처리되었습니다."));

        ClientEntity sender = chatRequest.getSenderNickname();
        ClientEntity receiver = chatRequest.getReceiverNickname();

        chatRequest.accept();
        chatRequestRepository.save(chatRequest);

        Long roomId = createChatRoom(sender, receiver);

        return roomId;
    }

    public void rejectChatRequest(Long requestId) {
        ChatRequest chatRequest = chatRequestRepository.findByIdAndStatus(requestId, RequestStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청이 없거나 이미 처리되었습니다."));

        chatRequest.reject();
        chatRequestRepository.save(chatRequest);
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

        long remainingUsers = chatJoinRepository.countByChatRoomAndExitedFalse(chatRoom);

        if (remainingUsers == 0) {
            chatRoomRepository.delete(chatRoom);
        }
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
