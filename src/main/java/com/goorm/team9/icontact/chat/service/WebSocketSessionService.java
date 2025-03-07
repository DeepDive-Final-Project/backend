package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.chat.dto.ChatMessageDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionService {

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<Long, Map<String, WebSocketSession>> chatRoomSessions = new ConcurrentHashMap<>();

    public WebSocketSessionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void addSession(Long chatRoomId, String senderNickname, WebSocketSession session) {
        chatRoomSessions
                .computeIfAbsent(chatRoomId, k -> new ConcurrentHashMap<>())
                .put(senderNickname, session);
        sendJoinMessage(chatRoomId, senderNickname);
    }

    public void removeSession(Long chatRoomId, String senderNickname) {
        Map<String, WebSocketSession> usersInRoom = chatRoomSessions.get(chatRoomId);
        if (usersInRoom != null) {
            usersInRoom.remove(senderNickname);
            sendLeaveMessage(chatRoomId, senderNickname);
        }
    }

    private void sendJoinMessage(Long chatRoomId, String senderNickname) {
        ChatMessageDto joinMessage = ChatMessageDto.createJoinMessage(chatRoomId, senderNickname);
        messagingTemplate.convertAndSend("/queue/" + chatRoomId, joinMessage);
    }

    private void sendLeaveMessage(Long chatRoomId, String senderNickname) {
        ChatMessageDto leaveMessage = ChatMessageDto.createLeaveMessage(chatRoomId, senderNickname);
        messagingTemplate.convertAndSend("/queue/" + chatRoomId, leaveMessage);
    }

    public void sendMessageToChatRoom(Long chatRoomId, String message) {
        Map<String, WebSocketSession> usersInRoom = chatRoomSessions.get(chatRoomId);
        if (usersInRoom != null) {
            for (WebSocketSession session : usersInRoom.values()) {
                messagingTemplate.convertAndSendToUser(session.getId(), "/queue/" + chatRoomId, message);
            }
        }
    }
}
