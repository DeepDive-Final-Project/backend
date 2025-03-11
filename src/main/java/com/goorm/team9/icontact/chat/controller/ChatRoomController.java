package com.goorm.team9.icontact.chat.controller;

import com.goorm.team9.icontact.chat.dto.ChatRoomRequest;
import com.goorm.team9.icontact.chat.dto.ChatRoomResponse;
import com.goorm.team9.icontact.chat.service.ChatRoomService;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "채팅방 생성 API", description = "새로운 1:1 채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(@RequestBody ChatRoomRequest request) {
        try {
            ClientEntity senderNickname = clientRepository.findByNickName(request.getSenderNickname())
                    .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));
            ClientEntity receiverNickname = clientRepository.findByNickName(request.getReceiverNickname())
                    .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

            Long roomId = chatRoomService.createChatRoom(senderNickname, receiverNickname);

            ChatRoomResponse response = new ChatRoomResponse(
                    roomId,
                    List.of(senderNickname.getNickName(), receiverNickname.getNickName()),
                    null,null
            );
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(summary = "사용자의 채팅방 조회 API", description = "특정 사용자가 참여한 채팅방 목록을 조회합니다.")
    @GetMapping("/{nickname}")
    public ResponseEntity<List<ChatRoomResponse>> getChatRoomsByUser(@PathVariable String nickname) {
        try {
            ClientEntity client = clientRepository.findByNickName(nickname)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            List<ChatRoomResponse> chatRooms = chatRoomService.getChatRoomsByUser(client);
            if (chatRooms.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok().body(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "모든 채팅방 조회 API", description = "모든 채팅방을 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<List<ChatRoomResponse>> getAllChatRooms() {
        try {
            List<ChatRoomResponse> allChatRooms = chatRoomService.getAllChatRooms();
            if (allChatRooms.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok().body(allChatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "채팅방 퇴장 API", description = "사용자가 특정 채팅방을 나갑니다.")
    @PostMapping("/{roomId}/exit/{clientId}")
    public ResponseEntity<Void> exitChatRoom(@PathVariable Long roomId, @PathVariable Long clientId) {
        chatRoomService.exitChatRoom(roomId, clientId);
        return ResponseEntity.ok().build();
    }
}
