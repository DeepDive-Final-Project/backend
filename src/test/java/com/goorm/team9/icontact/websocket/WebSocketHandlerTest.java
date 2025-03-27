package com.goorm.team9.icontact.websocket;

import com.goorm.team9.icontact.config.websocket.WebSocketHandler;
import com.goorm.team9.icontact.domain.chat.service.WebSocketSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebSocketHandlerTest {

    private WebSocketSessionService webSocketSessionService;
    private WebSocketHandler webSocketHandler;

    @BeforeEach
    public void setUp() {
        webSocketSessionService = mock(WebSocketSessionService.class);
        webSocketHandler = new WebSocketHandler(webSocketSessionService);
    }

    @Test
    @DisplayName("WebSocket 연결 시 addSession 호출한다.")
    public void testAfterConnectionAddsession() throws Exception {
        // Given
        WebSocketSession session = mock(WebSocketSession.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("RoomId", 1L);
        attributes.put("senderNickname", "Noah1");

        when(session.getAttributes()).thenReturn(attributes);

        // When
        webSocketHandler.afterConnectionEstablished(session);

        // Then
        verify(webSocketSessionService).addSession(1L, "Noah1", session);
    }

    @Test
    @DisplayName("WebSocket 연결 종료 시 removeSession 호출한다.")
    public void testAfterConnectionRemovesession() throws Exception {
        // Given
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("chatRoomId", 1L);
        attributes.put("senderNickname", "Noah1");

        when(session.getAttributes()).thenReturn(attributes);

        // When
        webSocketHandler.afterConnectionClosed(session, CloseStatus.NORMAL);

        // Then
        verify(webSocketSessionService).removeSession(1L, "Noah1");
    }

}
