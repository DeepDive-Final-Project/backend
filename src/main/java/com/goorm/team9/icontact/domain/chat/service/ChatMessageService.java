package com.goorm.team9.icontact.domain.chat.service;

import com.goorm.team9.icontact.domain.chat.dto.ChatMessageDto;
import com.goorm.team9.icontact.domain.chat.entity.ChatJoin;
import com.goorm.team9.icontact.domain.chat.entity.ChatMessage;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.chat.repository.ChatJoinRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatMessageRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRoomRepository;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatJoinRepository chatJoinRepository;
    private final ClientRepository clientRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendMessage(ChatMessageDto chatMessageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ClientEntity senderNickname = clientRepository.findByNickName(chatMessageDto.getSenderNickname())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatJoin chatJoin = chatJoinRepository.findByChatRoomAndClientId(chatRoom, senderNickname.getId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여한 사용자가 아닙니다."));

        if (chatJoin.isExited()) {
            throw new IllegalArgumentException("채팅방을 나간 사용자는 메시지를 보낼 수 없습니다.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderNickname(senderNickname)
                .content(chatMessageDto.getContent())
                .type(chatMessageDto.getType())
                .build();
        chatMessageRepository.save(chatMessage);

        String destination = "/queue/" + chatMessageDto.getRoomId();
        messagingTemplate.convertAndSend(destination, chatMessageDto);
    }

    public List<ChatMessage> getMessages(Long roomId, Long clientId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatJoin chatJoin = chatJoinRepository.findByChatRoomAndClientId(chatRoom, clientId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 채팅방에 존재하지 않습니다."));

        if (chatJoin.isExited()) {
            throw new IllegalArgumentException("채팅방을 나간 사용자는 채팅 내역을 조회할 수 없습니다.");
        }

        return chatMessageRepository.findByChatRoom(chatRoom);
    }
}