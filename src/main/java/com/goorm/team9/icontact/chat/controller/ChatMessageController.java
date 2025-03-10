package com.goorm.team9.icontact.chat.controller;

import com.goorm.team9.icontact.chat.dto.ChatMessageDto;
import com.goorm.team9.icontact.chat.entity.ChatMessageType;
import com.goorm.team9.icontact.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto) {
        if (chatMessageDto.getType() == ChatMessageType.JOIN) {
            ChatMessageDto joinMessage = ChatMessageDto.createJoinMessage(
                    chatMessageDto.getChatRoomId(), chatMessageDto.getSenderNickname());
            chatMessageService.sendMessage(joinMessage);
            messagingTemplate.convertAndSend("/queue/" + chatMessageDto.getChatRoomId(), joinMessage);
        }

        else if (chatMessageDto.getType() == ChatMessageType.LEAVE) {
            ChatMessageDto leaveMessage = ChatMessageDto.createLeaveMessage(
                    chatMessageDto.getChatRoomId(), chatMessageDto.getSenderNickname());
            chatMessageService.sendMessage(leaveMessage);
            messagingTemplate.convertAndSend("/queue/" + chatMessageDto.getChatRoomId(), leaveMessage);
        }

        else {
            chatMessageService.sendMessage(chatMessageDto);
            messagingTemplate.convertAndSend("/queue/" + chatMessageDto.getChatRoomId(), chatMessageDto);
        }
    }
}