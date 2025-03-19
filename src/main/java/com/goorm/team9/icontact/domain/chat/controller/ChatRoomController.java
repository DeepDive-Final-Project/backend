package com.goorm.team9.icontact.domain.chat.controller;

import com.goorm.team9.icontact.domain.chat.dto.ChatRequestDto;
import com.goorm.team9.icontact.domain.chat.dto.ChatResponseDto;
import com.goorm.team9.icontact.domain.chat.dto.ChatRoomRequest;
import com.goorm.team9.icontact.domain.chat.dto.ChatRoomResponse;
import com.goorm.team9.icontact.domain.chat.service.ChatRoomService;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Chat Room API", description = "채팅방 생성 및 조회 API 입니다.")
@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ClientRepository clientRepository;

    public ChatRoomController(ChatRoomService chatRoomService, ClientRepository clientRepository) {
        this.chatRoomService = chatRoomService;
        this.clientRepository = clientRepository;
    }

    @Operation(summary = "채팅 신청 API", description = "상대방에게 채팅을 신청합니다.")
    @PostMapping("/request")
    public ResponseEntity<ChatResponseDto> requestChat(@RequestBody ChatRequestDto requestDto) {
        ClientEntity sender = clientRepository.findByNickName(requestDto.getSenderNickname())
                .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));
        ClientEntity receiver = clientRepository.findByNickName(requestDto.getReceiverNickname())
                .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

        Long requestId = chatRoomService.requestChat(sender, receiver);
        ChatResponseDto responseDto = new ChatResponseDto(requestId, "채팅이 요청되었습니다.");

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "채팅 승인 API", description = "채팅 신청을 승인하여 채팅방을 개설합니다.")
    @PostMapping("/request/accept")
    public ResponseEntity<Map<String, Object>> acceptChatRequest(
            @RequestBody
            @Schema(example = "{\"requestId\": 123}")
            Map<String, Long> request) {

        Map<String, Object> response = new HashMap<>();
        try {
            Long requestId = request.get("requestId");
            Long roomId = chatRoomService.acceptChatRequest(requestId);

            response.put("roomId", roomId);
            response.put("message", "채팅 요청이 승인되었습니다.");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "채팅 거절 API", description = "채팅 신청을 거절합니다.")
    @PostMapping("/request/reject")
    public ResponseEntity<Map<String, Object>> rejectChatRequest(
            @RequestBody
            @Schema(example = "{\"requestId\": 123}")
            Map<String, Long> request) {

        Long requestId = request.get("requestId");
        chatRoomService.rejectChatRequest(requestId);

        return ResponseEntity.ok(Map.of("message", "채팅 요청이 거절되었습니다."));
    }

    @Operation(summary = "채팅방 퇴장 API", description = "사용자가 특정 채팅방을 나갑니다.")
    @PostMapping("/exit")
    public ResponseEntity<Map<String, String>> exitChatRoom(
            @RequestBody
            @Schema(example = "{\"roomId\": 123, \"clientId\": 456}")
            Map<String, Long> request) {

        Long roomId = request.get("roomId");
        Long clientId = request.get("clientId");

        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        chatRoomService.exitChatRoom(roomId, clientId);

        Map<String, String> response = new HashMap<>();
        response.put("message", client.getNickName() + "님이 퇴장했습니다.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채팅방 입장 API", description = "사용자가 채팅방에 입장하면 last_read_at을 업데이트합니다.")
    @PostMapping("/enter")
    public ResponseEntity<Map<String, String>> enterChatRoom(
            @RequestBody @Schema(example = "{\"roomId\": 123, \"clientId\": 456}") Map<String, Long> request) {

        Long roomId = request.get("roomId");
        Long clientId = request.get("clientId");

        chatRoomService.updateLastReadAt(roomId, clientId);

        return ResponseEntity.ok(Map.of("message", "last_read_at이 업데이트되었습니다."));
    }

    @Operation(summary = "최신 메시지 순으로 채팅방 조회", description = "특정 사용자가 참여한 채팅방을 최신 메시지 순으로 조회합니다.")
    @GetMapping("/latest")
    public ResponseEntity<List<ChatRoomResponse>> getLatestChatRooms(@RequestParam String nickname) {
        try {
            ClientEntity client = clientRepository.findByNickName(nickname)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            List<ChatRoomResponse> chatRooms = chatRoomService.getLatestChatRooms(client);

            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(summary = "읽지 않은 메시지가 있는 채팅방 조회", description = "특정 사용자가 참여한 채팅방 중 최근 메시지가 읽지 않은 순으로 정렬하여 조회합니다.")
    @GetMapping("/unread")
    public ResponseEntity<List<ChatRoomResponse>> getUnreadChatRooms(@RequestParam String nickname) {
        try {
            ClientEntity client = clientRepository.findByNickName(nickname)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            List<ChatRoomResponse> chatRooms = chatRoomService.getUnreadChatRooms(client);

            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(summary = "사용자의 채팅방 조회 API", description = "특정 사용자가 참여한 채팅방 목록을 조회합니다.")
    @GetMapping("/{nickname}")
    public ResponseEntity<Object> getChatRoomsByUser(@PathVariable String nickname) {
        try {
            ClientEntity client = clientRepository.findByNickName(nickname)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            List<ChatRoomResponse> chatRooms = chatRoomService.getChatRoomsByUser(client);

            if (chatRooms.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "사용자가 속해있는 채팅방이 없습니다."));
            }
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 내부 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "모든 채팅방 조회 API", description = "모든 채팅방을 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<List<ChatRoomResponse>> getAllChatRooms(@RequestParam Long clientId) {
        try {
            ClientEntity client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            List<ChatRoomResponse> allChatRooms = chatRoomService.getAllChatRooms(client);

            if (allChatRooms.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok().body(allChatRooms);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "안 읽은 메시지 개수 조회 API", description = "특정 채팅방에서 사용자의 안 읽은 메시지 개수를 반환합니다.")
    @GetMapping("/{roomId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount(@PathVariable Long chatRoomId, @RequestParam Long clientId) {
        long unreadCount = chatRoomService.countUnreadMessages(chatRoomId, clientId);
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @Operation(summary = "채팅방 생성 API", description = "새로운 1:1 채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createChatRoom(@RequestBody ChatRoomRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            ClientEntity senderNickname = clientRepository.findByNickName(request.getSenderNickname())
                    .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));
            ClientEntity receiverNickname = clientRepository.findByNickName(request.getReceiverNickname())
                    .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

            Long roomId = chatRoomService.createChatRoom(senderNickname, receiverNickname);

            Map<String, Object> data = new HashMap<>();
            data.put("roomId", roomId);
            data.put("senderNickname", senderNickname.getNickName());
            data.put("receiverNickname", receiverNickname.getNickName());

            response.put("data", data);
            response.put("message", "채팅방이 생성되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}