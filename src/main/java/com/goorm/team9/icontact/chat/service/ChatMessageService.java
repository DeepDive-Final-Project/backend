package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.chat.dto.ChatMessageDto;
import com.goorm.team9.icontact.chat.entity.ChatMessage;
import com.goorm.team9.icontact.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendMessage(ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(chatMessageDto.getRoomId())
                .senderNickname(chatMessageDto.getSenderNickname())
                .content(chatMessageDto.getContent())
                .type(chatMessageDto.getType())
                .build();
        chatMessageRepository.save(chatMessage);

        String destination = "/queue/" + chatMessageDto.getRoomId();
        messagingTemplate.convertAndSend(destination, chatMessageDto);
    }
}