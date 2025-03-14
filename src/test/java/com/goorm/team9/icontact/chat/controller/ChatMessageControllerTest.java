package com.goorm.team9.icontact.chat.controller;

import com.goorm.team9.icontact.domain.chat.controller.ChatMessageController;
import com.goorm.team9.icontact.domain.chat.dto.ChatMessageDto;
import com.goorm.team9.icontact.domain.chat.service.ChatMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatMessageController chatMessageController;

    @Test
    @DisplayName("채팅 메시지 전송 테스트")
    public void testSendMessage() {
        // Given
        Long chatRoomId = 1L;
        String senderNickname = "UserA";
        String content = "안녕!";
        ChatMessageDto chatMessageDto = ChatMessageDto.createChatMessage(chatRoomId, senderNickname, content);

        // When
        chatMessageController.sendMessage(chatMessageDto);

        // Then
        verify(chatMessageService).sendMessage(chatMessageDto);

        verify(messagingTemplate).convertAndSend("/queue/" + chatRoomId, chatMessageDto);
    }
}
