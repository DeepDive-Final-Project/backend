package com.goorm.team9.icontact.config.websocket;

import com.goorm.team9.icontact.chat.service.WebSocketSessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionService webSocketSessionService;

    public WebSocketHandler(WebSocketSessionService webSocketSessionService) {
        this.webSocketSessionService = webSocketSessionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long chatRoomId = (Long) session.getAttributes().get("chatRoomId");
        String senderNickname = (String) session.getAttributes().get("senderNickname");
        webSocketSessionService.addSession(chatRoomId, senderNickname, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long chatRoomId = (Long) session.getAttributes().get("chatRoomId");
        String senderNickname = (String) session.getAttributes().get("senderNickname");
        webSocketSessionService.removeSession(chatRoomId, senderNickname);
    }
}

