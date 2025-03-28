package com.goorm.team9.icontact.domain.chat.service;

import com.goorm.team9.icontact.domain.chat.dto.ChatRoomResponse;
import com.goorm.team9.icontact.domain.chat.entity.ChatJoin;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.chat.repository.ChatJoinRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatMessageRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRequestRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRoomRepository;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.client.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatJoinRepository chatJoinRepository;
    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final ChatRequestRepository chatRequestRepository;
    private final ChatMessageRepository chatMessageRepository;

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
    public void exitChatRoom(Long roomId, Long clientId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatJoin chatJoin = chatJoinRepository.findByChatRoomAndClientId(chatRoom, clientId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 채팅방에 존재하지 않습니다."));

        if (chatJoin.isExited()) {
            return;
        }

        chatJoin.exitChatRoom();
        chatJoinRepository.save(chatJoin);

        long remainingUsers = chatJoinRepository.countByChatRoomAndExitedFalse(chatRoom);

        if (remainingUsers == 0) {
            chatJoinRepository.deleteAll(chatJoinRepository.findByChatRoom(chatRoom));
            chatRoomRepository.delete(chatRoom);
        }
    }

    public List<ChatRoomResponse> getLatestChatRooms(ClientEntity client) {
        List<Object[]> results = chatRoomRepository.findAllChatRoomsWithUnreadCount(client.getNickName(), client.getId());

        return results.stream()
                .map(result -> {
                    ChatRoom chatRoom = (ChatRoom) result[0];
                    Long unreadCount = (Long) result[1];
                    boolean exited = chatJoinRepository.findByChatRoomAndClientId(chatRoom, client.getId())
                            .map(ChatJoin::isExited)
                            .orElse(true);

                    return ChatRoomResponse.fromEntity(chatRoom, unreadCount, exited, client.getId());
                })
                .collect(Collectors.toList());
    }

    public List<ChatRoomResponse> getUnreadChatRooms(ClientEntity client) {
        List<Object[]> results = chatRoomRepository.findUnreadChatRoomsWithUnreadCount(client.getNickName(), client.getId());

        return results.stream()
                .map(result -> {
                    ChatRoom chatRoom = (ChatRoom) result[0];
                    Long unreadCount = (Long) result[1];
                    boolean exited = chatJoinRepository.findByChatRoomAndClientId(chatRoom, client.getId())
                            .map(ChatJoin::isExited)
                            .orElse(true);

                    return ChatRoomResponse.fromEntity(chatRoom, unreadCount, exited, client.getId());
                })
                .collect(Collectors.toList());
    }

    public List<ChatRoomResponse> getChatRoomsByUser(ClientEntity client) {
        List<Object[]> results = chatRoomRepository.findAllChatRoomsWithUnreadCount(client.getNickName(), client.getId());

        return results.stream()
                .map(result -> {
                    ChatRoom chatRoom = (ChatRoom) result[0];
                    Long unreadCount = (Long) result[1];
                    boolean exited = chatJoinRepository.findByChatRoomAndClientId(chatRoom, client.getId())
                            .map(ChatJoin::isExited)
                            .orElse(true);

                    return ChatRoomResponse.fromEntity(chatRoom, unreadCount, exited, client.getId());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateLastReadAt(Long roomId, Long clientId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ChatJoin chatJoin = chatJoinRepository.findByChatRoomAndClientId(chatRoom, clientId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 채팅방에 존재하지 않습니다."));

        chatJoin.updateLastReadAt();
        chatJoinRepository.save(chatJoin);
    }

    @Transactional(readOnly = true)
    public long countUnreadMessages(Long roomId, Long clientId) {
        return chatMessageRepository.countUnreadMessages(roomId, clientId);
    }
}