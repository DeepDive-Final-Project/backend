package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.chat.dto.ChatMessageDto;
import com.goorm.team9.icontact.chat.entity.ChatRoom;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;

    private final Map<Long, Map<String, WebSocketSession>> chatRoomSessions = new ConcurrentHashMap<>();

    public WebSocketSessionService(SimpMessagingTemplate messagingTemplate, ChatRoomService chatRoomService) {
        this.messagingTemplate = messagingTemplate;
        this.chatRoomService = chatRoomService;
    }

    public void addSession(Long roomId, String senderNickname, WebSocketSession session) {
        chatRoomSessions
                .computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                .put(senderNickname, session);
        sendJoinMessage(roomId, senderNickname);
    }

    public void removeSession(Long roomId, String senderNickname) {
        Map<String, WebSocketSession> usersInRoom = chatRoomSessions.get(roomId);
        if (usersInRoom != null) {
            usersInRoom.remove(senderNickname);
            sendLeaveMessage(roomId, senderNickname);
        }
    }

    private void sendJoinMessage(Long roomId, String senderNickname) {
        ChatMessageDto joinMessage = ChatMessageDto.createJoinMessage(roomId, senderNickname);
        messagingTemplate.convertAndSend("/queue/" + roomId, joinMessage);
    }

    private void sendLeaveMessage(Long roomId, String senderNickname) {
        ChatMessageDto leaveMessage = ChatMessageDto.createLeaveMessage(roomId, senderNickname);
        messagingTemplate.convertAndSend("/queue/" + roomId, leaveMessage);
    }

    public void sendMessageToChatRoom(Long roomId, String message) {
        Map<String, WebSocketSession> usersInRoom = chatRoomSessions.get(roomId);
        if (usersInRoom != null) {
            for (WebSocketSession session : usersInRoom.values()) {
                messagingTemplate.convertAndSendToUser(session.getId(), "/queue/" + roomId, message);
            }
        }
    }

    public Long createOrGetRoomId(String senderNickname, String receiverNickname) {
        return chatRoomService.createOrGetRoomId(senderNickname, receiverNickname);
    }
}
