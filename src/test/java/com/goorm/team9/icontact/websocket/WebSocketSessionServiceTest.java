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
import com.goorm.team9.icontact.domain.chat.dto.response.ChatMessageDto;

@ExtendWith(MockitoExtension.class)
public class WebSocketSessionServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatRoomService chatRoomService;

    @Test
    @DisplayName("사용자가 채팅방에 입장했을 때 입장 메시지를 전송한다.")
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
        assertEquals("UserA님과의 대화가 시작되었어요.", capturedMessage.getContent());
    }

    @Test
    @DisplayName("사용자가 채팅방을 나갔을 때 퇴장 메시지를 전송한다.")
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

    @Test
    @DisplayName("채팅방에 속한 모든 사용자에게 메시지를 전송한다.")
    public void testSendMessageToChatRoom() {
        // Given
        Long chatRoomId = 1L;
        String user1 = "Noah1";
        String user2 = "Noah2";
        String message = "안녕하세요.";

        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);

        when(session1.getId()).thenReturn("session1");
        when(session2.getId()).thenReturn("session2");

        WebSocketSessionService service = new WebSocketSessionService(messagingTemplate, chatRoomService);
        service.addSession(chatRoomId, user1, session1);
        service.addSession(chatRoomId, user2, session2);

        // When
        service.sendMessageToChatRoom(chatRoomId, message);

        // Then
        verify(messagingTemplate).convertAndSendToUser("session1", "/queue/" + chatRoomId, message);
        verify(messagingTemplate).convertAndSendToUser("session2", "/queue/" + chatRoomId, message);
    }

    @Test
    @DisplayName("특정 사용자에게 메시지를 전송한다.")
    public void testSendPrivateMessage() {
        // Given
        String nickname = "Noah1";
        String destination = "/queue/personal";
        String payload = "테스트 메시지입니다.";

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(session.getId()).thenReturn("session1");

        WebSocketSessionService service = new WebSocketSessionService(messagingTemplate, chatRoomService);

        Long chatRoomId = 1L;
        service.addSession(chatRoomId, nickname, session);

        // When
        service.sendPrivateMessage(nickname, destination, payload);

        // Then
        verify(messagingTemplate).convertAndSendToUser("session1", destination, payload);
    }

    @Test
    @DisplayName("사용자가 온라인에 접속해있는지 확인한다.")
    public void testIsUserOnline() {
        // Given
        String nickname = "Noah1";
        WebSocketSession session = mock(WebSocketSession.class);
        Long roomId = 1L;

        WebSocketSessionService service = new WebSocketSessionService(messagingTemplate, chatRoomService);
        service.addSession(roomId, nickname, session);

        // When
        boolean isOnline = service.isUserOnline(nickname);

        // Then
        assertEquals(true, isOnline);
    }

    @Test
    @DisplayName("사용자가 오프라인 상태인지 확인한다.")
    public void testIsUserOffline() {
        // Given
        String nickname = "Noah1000";
        WebSocketSessionService service = new WebSocketSessionService(messagingTemplate, chatRoomService);

        // When
        boolean isOnline = service.isUserOnline(nickname);

        // Then
        assertEquals(false, isOnline);
    }
}