package com.goorm.team9.icontact.config.websocket;

import com.goorm.team9.icontact.domain.chat.service.WebSocketSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Component
public class WebSocketConnectListener implements ApplicationListener<SessionConnectedEvent> {

    @Autowired
    private WebSocketSessionService webSocketSessionService;

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        Long roomId = Long.parseLong(headerAccessor.getFirstNativeHeader("roomId"));
        Long clientId = Long.parseLong(headerAccessor.getFirstNativeHeader("clientId"));
        String nickname = headerAccessor.getFirstNativeHeader("senderNickname");

        webSocketSessionService.addStompSession(roomId, clientId, nickname);
    }

}
