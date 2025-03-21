package com.goorm.team9.icontact.domain.chat.controller;

import com.goorm.team9.icontact.domain.chat.dto.ChatRequestCountDto;
import com.goorm.team9.icontact.domain.chat.dto.ChatRequestDto;
import com.goorm.team9.icontact.domain.chat.dto.ChatResponseDto;
import com.goorm.team9.icontact.domain.chat.entity.ChatRequest;
import com.goorm.team9.icontact.domain.chat.entity.RequestStatus;
import com.goorm.team9.icontact.domain.chat.service.ChatRequestService;
import com.goorm.team9.icontact.domain.chat.service.ChatRoomService;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Chat Request API", description = "채팅 요청 API")
@RestController
@RequestMapping("/api/chat/request")
public class ChatRequestController {

    private final ChatRoomService chatRoomService;
    private final ClientRepository clientRepository;
    private final ChatRequestService chatRequestService;

    public ChatRequestController(ChatRoomService chatRoomService, ClientRepository clientRepository, ChatRequestService chatRequestService) {
        this.chatRoomService = chatRoomService;
        this.clientRepository = clientRepository;
        this.chatRequestService = chatRequestService;
    }

    @Operation(summary = "채팅 요청 API", description = "상대방에게 채팅을 요청을 보냅니다.")
    @PostMapping
    public ResponseEntity<ChatResponseDto> requestChat(@RequestBody ChatRequestDto requestDto) {
        ClientEntity sender = clientRepository.findByNickName(requestDto.getSenderNickname())
                .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));
        ClientEntity receiver = clientRepository.findByNickName(requestDto.getReceiverNickname())
                .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

        return chatRequestService.requestChat(sender, receiver);
    }

    @Operation(summary = "채팅 요청 상태 확인", description = "요청 상태를 확인합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ChatResponseDto> getChatRequest(@PathVariable Long id) {
        Optional<ChatRequest> chatRequest = chatRequestService.getChatRequestById(id);
        if (chatRequest.isPresent()) {
            return ResponseEntity.ok(new ChatResponseDto(chatRequest.get().getId(), chatRequest.get().getStatus().toString(), null));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "채팅 승인 API", description = "채팅 신청을 승인하여 채팅방을 개설합니다.")
    @PatchMapping("/accept")
    public ResponseEntity<ChatResponseDto> acceptChatRequest(@PathVariable Long id) {
        Long roomId = chatRequestService.acceptChatRequest(id);
        return ResponseEntity.ok(new ChatResponseDto(id, "채팅 요청이 수락되었습니다.", roomId));
    }

    @Operation(summary = "채팅 거절 API", description = "채팅 신청을 거절합니다.")
    @PatchMapping("/reject")
    public ResponseEntity<ChatResponseDto> rejectChatRequest(@PathVariable Long id) {
        chatRequestService.rejectChatRequest(id);
        return ResponseEntity.ok(new ChatResponseDto(id, "채팅 요청이 거절되었습니다.", null));
    }

    @Operation(summary = "받은 채팅 요청 조회 API", description = "사용자가 받은 채팅 요청을 조회합니다.")
    @GetMapping("/received")
    public ResponseEntity<List<ChatRequestDto>> getReceivedRequests(
            @RequestParam String receiverNickname,
            @RequestParam RequestStatus status) {
        List<ChatRequestDto> receivedRequests = chatRequestService.getReceivedRequests(receiverNickname, status);
        return ResponseEntity.ok(receivedRequests);
    }

    @Operation(summary = "보낸 채팅 요청 조회 API", description = "사용자가 보낸 채팅 요청을 조회합니다.")
    @GetMapping("/sent")
    public ResponseEntity<List<ChatRequestDto>> getSentRequests(
            @RequestParam String senderNickname,
            @RequestParam RequestStatus status) {
        List<ChatRequestDto> sentRequests = chatRequestService.getSentRequest(senderNickname, status);
        return ResponseEntity.ok(sentRequests);
    }

    @Operation(summary = "요청 개수 조회 API", description = "사용자가 받은 요청과 보낸 요청의 개수를 반환합니다.")
    @GetMapping("/count")
    public ResponseEntity<ChatRequestCountDto> getRequestCounts(@RequestParam String nickname) {
        ChatRequestCountDto countDto = chatRequestService.getRequestCounts(nickname);
        return ResponseEntity.ok(countDto);
    }
}
