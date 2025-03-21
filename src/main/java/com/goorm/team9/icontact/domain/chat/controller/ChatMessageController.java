package com.goorm.team9.icontact.domain.chat.controller;

import com.goorm.team9.icontact.domain.chat.dto.ChatMessageDto;
import com.goorm.team9.icontact.domain.chat.entity.ChatMessageType;
import com.goorm.team9.icontact.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto) {
        try {
            if (chatMessageDto.getContent().length() > 1000) {
                throw new IllegalArgumentException("채팅 메시지는 최대 1000자까지 입력할 수 있습니다.");
            }

            if (chatMessageDto.getType() == ChatMessageType.JOIN) {
                ChatMessageDto joinMessage = ChatMessageDto.createJoinMessage(
                        chatMessageDto.getRoomId(), chatMessageDto.getSenderNickname());
                chatMessageService.sendMessage(joinMessage);
                messagingTemplate.convertAndSend("/queue/" + chatMessageDto.getRoomId(), joinMessage);
            }

            else if (chatMessageDto.getType() == ChatMessageType.LEAVE) {
                ChatMessageDto leaveMessage = ChatMessageDto.createLeaveMessage(
                        chatMessageDto.getRoomId(), chatMessageDto.getSenderNickname());
                chatMessageService.sendMessage(leaveMessage);
                messagingTemplate.convertAndSend("/queue/" + chatMessageDto.getRoomId(), leaveMessage);
            }

            else {
                chatMessageService.sendMessage(chatMessageDto);
                messagingTemplate.convertAndSend("/queue/" + chatMessageDto.getRoomId(), chatMessageDto);
            }
    } catch (IllegalArgumentException e) {
        String errorMessage = e.getMessage();
        messagingTemplate.convertAndSend("/queue/" + chatMessageDto.getRoomId(), errorMessage);
        }
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessagesByRoomId(
            @PathVariable Long roomId,
            @RequestParam Long clientId) {

        List<ChatMessageDto> messages = chatMessageService.getMessagesByRoomId(roomId, clientId);
        return ResponseEntity.ok(messages);
    }
}