package com.goorm.team9.icontact.chat.controller;

import com.goorm.team9.icontact.chat.dto.ChatRoomRequest;
import com.goorm.team9.icontact.chat.dto.ChatRoomResponse;
import com.goorm.team9.icontact.chat.service.ChatRoomService;
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

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @Operation(summary = "채팅방 생성 API", description = "새로운 1:1 채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(@RequestBody ChatRoomRequest request) {
        try {
            Long roomId = chatRoomService.createChatRoom(request.getSenderNickname(), request.getReceiverNickname());

            ChatRoomResponse response = new ChatRoomResponse(
                    roomId,
                    List.of(request.getSenderNickname(), request.getReceiverNickname()),
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
            List<ChatRoomResponse> chatRooms = chatRoomService.getChatRoomsByUser(nickname);
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
}
