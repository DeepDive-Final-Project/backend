package com.goorm.team9.icontact.domain.chat.controller;

import com.goorm.team9.icontact.domain.chat.dto.ChatRequestDto;
import com.goorm.team9.icontact.domain.chat.dto.ChatResponseDto;
import com.goorm.team9.icontact.domain.chat.entity.ChatRequest;
import com.goorm.team9.icontact.domain.chat.service.ChatRoomService;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Chat Request API", description = "채팅 요청 API")
@RestController
@RequestMapping("/api/chat/request")
public class ChatRequestController {

    private final ChatRoomService chatRoomService;
    private final ClientRepository clientRepository;

    public ChatRequestController(ChatRoomService chatRoomService, ClientRepository clientRepository) {
        this.chatRoomService = chatRoomService;
        this.clientRepository = clientRepository;
    }

    @Operation(summary = "채팅 요청 API", description = "상대방에게 채팅을 요청을 보냅니다.")
    @PostMapping
    public ResponseEntity<ChatResponseDto> requestChat(@RequestBody ChatRequestDto requestDto) {
        ClientEntity sender = clientRepository.findByNickName(requestDto.getSenderNickname())
                .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));
        ClientEntity receiver = clientRepository.findByNickName(requestDto.getReceiverNickname())
                .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

        return chatRoomService.requestChat(sender, receiver);
    }

    @Operation(summary = "채팅 요청 상태 확인", description = "요청 상태를 확인합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ChatResponseDto> getChatRequest(@PathVariable Long id) {
        Optional<ChatRequest> chatRequest = chatRoomService.getChatRequestById(id);
        if (chatRequest.isPresent()) {
            return ResponseEntity.ok(new ChatResponseDto(chatRequest.get().getId(), chatRequest.get().getStatus().toString()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "채팅 승인 API", description = "채팅 신청을 승인하여 채팅방을 개설합니다.")
    @PatchMapping("/accept")
    public ResponseEntity<ChatResponseDto> acceptChatRequest(@PathVariable Long id) {
        Long roomId = chatRoomService.acceptChatRequest(id);
        return ResponseEntity.ok(new ChatResponseDto(roomId, "채팅 요청이 수락되었습니다."));
    }

    @Operation(summary = "채팅 거절 API", description = "채팅 신청을 거절합니다.")
    @PatchMapping("/reject")
    public ResponseEntity<ChatResponseDto> rejectChatRequest(@PathVariable Long id) {
        chatRoomService.rejectChatRequest(id);
        return ResponseEntity.ok(new ChatResponseDto(id, "채팅 요청이 거절되었습니다."));
    }
}
