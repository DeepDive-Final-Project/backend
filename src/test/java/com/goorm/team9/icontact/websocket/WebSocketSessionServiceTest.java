package com.goorm.team9.icontact.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.goorm.team9.icontact.domain.chat.service.ChatRoomService;
import com.goorm.team9.icontact.domain.chat.service.WebSocketSessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.WebSocketSession;
import com.goorm.team9.icontact.domain.chat.dto.ChatMessageDto;

@ExtendWith(MockitoExtension.class)
public class WebSocketSessionServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatRoomService chatRoomService;

    @Test
    @DisplayName("사용자가 채팅방에 입장했을 때 입장 메시지 전송 테스트")
    public void testAddSession() throws Exception {
        // Given
        WebSocketSession session = mock(WebSocketSession.class);
        Long chatRoomId = 1L;
        String senderNickname = "UserA";

        WebSocketSessionService webSocketSessionService = new WebSocketSessionService(messagingTemplate, chatRoomService);

        // When
        webSocketSessionService.addSession(chatRoomId, senderNickname, session);

        // Then
        ArgumentCaptor<ChatMessageDto> captor = ArgumentCaptor.forClass(ChatMessageDto.class);
        verify(messagingTemplate).convertAndSend(eq("/queue/" + chatRoomId), captor.capture());

        // Verify
        ChatMessageDto capturedMessage = captor.getValue();
        assertEquals("UserA님이 입장했습니다.", capturedMessage.getContent());
    }

    @Test
    @DisplayName("사용자가 채팅방을 나갔을 때 퇴장 메시지 전송 테스트")
    public void testRemoveSession() throws Exception {
        // Given
        WebSocketSession session = mock(WebSocketSession.class);
        Long chatRoomId = 1L;
        String senderNickname = "UserA";

        WebSocketSessionService webSocketSessionService = new WebSocketSessionService(messagingTemplate, chatRoomService);

        webSocketSessionService.addSession(chatRoomId, senderNickname, session);

        // When
        webSocketSessionService.removeSession(chatRoomId, senderNickname);

        // Then
        ArgumentCaptor<ChatMessageDto> captor = ArgumentCaptor.forClass(ChatMessageDto.class);

        // Verify
        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/queue/" + chatRoomId), captor.capture());

        ChatMessageDto capturedMessage = captor.getValue();
        assertEquals("UserA님이 퇴장했습니다.", capturedMessage.getContent());

        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/queue/" + chatRoomId), captor.capture());
    }
}