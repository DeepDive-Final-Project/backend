package com.goorm.team9.icontact.domain.chat.service;

import com.goorm.team9.icontact.domain.block.repository.BlockRepository;
import com.goorm.team9.icontact.domain.chat.dto.ChatMessageDto;
import com.goorm.team9.icontact.domain.chat.dto.ChatMessageNotificationDto;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatJoinRepository chatJoinRepository;
    private final ClientRepository clientRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final BlockRepository blockRepository;
    private final WebSocketSessionService webSocketSessionService;
    private final EmailService emailService;

    @Transactional
    public void sendMessage(ChatMessageDto chatMessageDto) {
        if (chatMessageDto.getContent().length() > 1000) {
            throw new IllegalArgumentException("채팅 메시지는 최대 1000자까지 입력할 수 있습니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ClientEntity senderNickname = clientRepository.findByNickName(chatMessageDto.getSenderNickname())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatJoin chatJoin = chatJoinRepository.findByChatRoomAndClientId(chatRoom, senderNickname.getId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여한 사용자가 아닙니다."));

        if (chatJoin.isExited()) {
            throw new IllegalArgumentException("채팅방을 나간 사용자는 메시지를 보낼 수 없습니다.");
        }

        List<ChatJoin> participants = chatJoinRepository.findByChatRoom(chatRoom);
        for (ChatJoin participant : participants) {
            ClientEntity recipient = participant.getClient();
            if (blockRepository.isUserBlocked(senderNickname, recipient) || blockRepository.isUserBlocked(recipient, senderNickname)) {
                throw new IllegalArgumentException("차단된 사용자와 메시지를 주고받을 수 없습니다.");
            }
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderNickname(senderNickname.getNickName())
                .content(chatMessageDto.getContent())
                .type(chatMessageDto.getType())
                .build();
        chatMessageRepository.save(chatMessage);

        chatRoom.updateLastMessage(chatMessage.getContent(), chatMessage.getCreated_at());
        chatRoomRepository.save(chatRoom);

        String destination = "/queue/" + chatMessageDto.getRoomId();
        messagingTemplate.convertAndSend(destination, chatMessageDto);

        for (ChatJoin participant : participants) {
            ClientEntity recipient = participant.getClient();
            if (!recipient.getNickName().equals(chatMessageDto.getSenderNickname())) {
                String userDestination = "/queue/chat-notification/" + recipient.getNickName();

                ChatMessageNotificationDto notification = new ChatMessageNotificationDto(
                        chatMessageDto.getSenderNickname(),
                        chatMessageDto.getContent().length() > 30 ? chatMessageDto.getContent().substring(0, 30) + "..." : chatMessageDto.getContent(),
                        chatRoom.getRoomId(),
                        chatMessage.getCreated_at()
                );

                messagingTemplate.convertAndSend(userDestination, notification);

                if (!webSocketSessionService.isUserOnline(recipient.getNickName())) {
                    String recipientEmail = recipient.getEmail();
                    String messagePreview = chatMessageDto.getContent().length() > 30
                            ? chatMessageDto.getContent().substring(0, 30) + "..."
                            : chatMessageDto.getContent();

                    emailService.sendNewMessageNotification(recipientEmail, chatMessageDto.getSenderNickname(), messagePreview);
                }
            }
        }
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

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessagesByRoomId(Long roomId, Long clientId) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방을 찾을 수 없습니다."));

        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean isMember = chatJoinRepository.existsByChatRoomAndClientId(chatRoom, clientId);
        if (!isMember) {
            throw new IllegalArgumentException("해당 채팅방에 속해있지 않습니다.");
        }

        return chatMessageRepository.findByChatRoomId(roomId)
                .stream()
                .map(ChatMessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(Long roomId, Long clientId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ClientEntity reader = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(chatRoom, reader);

        for (ChatMessage chatMessage : unreadMessages) {
            chatMessage.markAsRead();
        }

        chatMessageRepository.saveAll(unreadMessages);
    }
}