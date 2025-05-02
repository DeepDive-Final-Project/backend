package com.goorm.team9.icontact.domain.chat.controller;

import com.goorm.team9.icontact.domain.chat.dto.response.ChatMessageDto;
import com.goorm.team9.icontact.domain.chat.entity.ChatMessageType;
import com.goorm.team9.icontact.domain.chat.service.ChatMessageService;
import com.goorm.team9.icontact.domain.chat.service.WebSocketSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import java.util.List;

@Tag(name = "Chat Message API", description = "채팅 메시지 관련 API")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final WebSocketSessionService webSocketSessionService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            try {
                String errorJson = objectMapper.writeValueAsString(Map.of(
                        "type", "error",
                        "message", e.getMessage()
                ));
                messagingTemplate.convertAndSend("/queue/" + chatMessageDto.getRoomId(), errorJson);
            } catch (Exception jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    @Operation(summary = "채팅 메시지 목록 조회", description = "해당 채팅방의 메시지를 조회합니다.")
    @GetMapping("/{roomId}")
    public ResponseEntity<List<ChatMessageDto>> getMessagesByRoomId(
            @PathVariable Long roomId,
            @RequestParam Long clientId) {

        List<ChatMessageDto> messages = chatMessageService.getMessagesByRoomId(roomId, clientId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "메시지 읽음 처리", description = "해당 채팅방에서 사용자의 메시지를 읽음 처리합니다.")
    @PatchMapping("/{roomId}/read")
    public ResponseEntity<Void> markMessagesRead(@PathVariable Long roomId, @RequestParam Long clientId) {
        chatMessageService.markMessagesAsRead(roomId, clientId);
        return ResponseEntity.noContent().build();
    }

    @MessageMapping("/chat.enter")
    public void enterStomp(@Payload Map<String, Object> payload) {
        Long roomId = Long.parseLong(payload.get("roomId").toString());
        Long clientId = Long.parseLong(payload.get("clientId").toString());
        String nickname = payload.get("senderNickname").toString();

        webSocketSessionService.addStompSession(roomId, clientId, nickname);
    }

}
