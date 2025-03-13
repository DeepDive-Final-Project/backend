//package com.goorm.team9.icontact.chat.service;
//
//import com.goorm.team9.icontact.chat.dto.ChatMessageDto;
//import com.goorm.team9.icontact.chat.entity.ChatMessage;
//import com.goorm.team9.icontact.chat.entity.ChatMessageType;
//import com.goorm.team9.icontact.chat.repository.ChatMessageRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.InjectMocks;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import static org.mockito.Mockito.verify;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class ChatMessageServiceTest {
//
//    @Mock
//    private ChatMessageRepository chatMessageRepository = Mockito.mock(ChatMessageRepository.class);
//
//    @Mock
//    private SimpMessagingTemplate messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
//
//    @InjectMocks
//    private ChatMessageService chatMessageService = new ChatMessageService(chatMessageRepository, messagingTemplate);
//
//    @Test
//    @DisplayName("채팅 메시지 전송 및 저장 테스트")
//    public void testSendMessage() {
//        // Given
//        Long chatRoomId = 1L;
//        String senderNickname = "UserA";
//        String content = "안녕!";
//        ChatMessageDto chatMessageDto = ChatMessageDto.createChatMessage(chatRoomId, senderNickname, content);
//
//        // When
//        chatMessageService.sendMessage(chatMessageDto);
//
//        // Then
//        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
//        verify(chatMessageRepository).save(captor.capture());
//
//        ChatMessage capturedMessage = captor.getValue();
//        assertEquals(chatRoomId, capturedMessage.getChatRoomId());
//        assertEquals(senderNickname, capturedMessage.getSenderNickname());
//        assertEquals(content, capturedMessage.getContent());
//        assertEquals(ChatMessageType.CHAT, capturedMessage.getType());
//
//        verify(messagingTemplate).convertAndSend("/queue/" + chatRoomId, chatMessageDto);
//    }
//}
