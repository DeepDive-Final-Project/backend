package com.goorm.team9.icontact.chat.controller;

import com.goorm.team9.icontact.chat.dto.ChatRoomDto;
import com.goorm.team9.icontact.chat.service.ChatRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @PostMapping
    public ResponseEntity<?> createChatRoom(@RequestBody ChatRoomDto.ChatRoomRequest request) {
        try {
            Long chatRoomId = chatRoomService.createChatRoom(request.getSenderNickname(), request.getReceiverNickname());

            ChatRoomDto.ChatRoomResponse response = new ChatRoomDto.ChatRoomResponse(chatRoomId, request.getSenderNickname(), request.getReceiverNickname());
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("채팅방 생성이 실패했습니다. (" + e.getMessage() + ")");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅방 생성 중 서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/{nickname}")
    public ResponseEntity<?> getChatRoomsByUser(@PathVariable String nickname) {
        try {
            var chatRooms = chatRoomService.getChatRoomsByUser(nickname);
            if (chatRooms.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("해당 사용자의 채팅방이 없습니다.");
            }
            return ResponseEntity.ok().body(chatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅방 검색 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllChatRooms() {
        try {
            var allChatRooms = chatRoomService.getAllChatRooms();
            if (allChatRooms.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("현재 존재하는 채팅방이 없습니다.");
            }
            return ResponseEntity.ok().body(allChatRooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("모든 채팅방 조회 중 오류가 발생했습니다.");
        }
    }
}
