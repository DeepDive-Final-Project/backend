package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.domain.block.repository.BlockRepository;
import com.goorm.team9.icontact.domain.chat.dto.response.ChatMessageDto;
import com.goorm.team9.icontact.domain.chat.entity.ChatJoin;
import com.goorm.team9.icontact.domain.chat.entity.ChatMessage;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.chat.repository.ChatJoinRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatMessageRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRoomRepository;
import com.goorm.team9.icontact.domain.chat.service.ChatMessageService;
import com.goorm.team9.icontact.domain.chat.service.EmailService;
import com.goorm.team9.icontact.domain.chat.service.WebSocketSessionService;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static com.goorm.team9.icontact.domain.chat.entity.ChatMessageType.CHAT;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ChatMessageServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatJoinRepository chatJoinRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private WebSocketSessionService webSocketSessionService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private ChatRoom chatRoom;
    private ClientEntity sender;
    private ClientEntity receiver;
    private ChatJoin senderJoin;
    private ChatJoin receiverJoin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sender = ClientEntity.builder().id(1L).nickName("Noah1").email("noah1@gmail.com").build();
        receiver = ClientEntity.builder().id(2L).nickName("Noah2").email("noah2@gmail.com").build();

        chatRoom = ChatRoom.createChatRoom(sender, receiver);
        chatRoom.setRoomId(100L);

        senderJoin = new ChatJoin();
        senderJoin.setClient(sender);
        senderJoin.setChatRoom(chatRoom);
        senderJoin.setExited(false);

        receiverJoin = new ChatJoin();
        receiverJoin.setClient(receiver);
        receiverJoin.setChatRoom(chatRoom);
        receiverJoin.setExited(false);
    }

    @Test
    @DisplayName("메시지 전송 시 DB에 저장되고 웹소켓으로 전송된다.")
    void sendMessage_success() {
        // Given
        ChatMessageDto messageDto = ChatMessageDto.builder()
                .roomId(100L)
                .senderNickname("Noah1")
                .content("안녕")
                .type(CHAT)
                .build();

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(chatRoom));
        when(clientRepository.findByNickName("Noah1")).thenReturn(Optional.of(sender));
        when(chatJoinRepository.findByChatRoomAndClientId(chatRoom, sender.getId()))
                .thenReturn(Optional.of(senderJoin));
        when(chatJoinRepository.findByChatRoom(chatRoom))
                .thenReturn(List.of(senderJoin, receiverJoin));
        when(webSocketSessionService.isUserOnline(anyString())).thenReturn(true);

        // When
        chatMessageService.sendMessage(messageDto);

        // Then
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/queue/" + messageDto.getRoomId()), eq(messageDto));
    }

    @Test
    @DisplayName("사용자가 오프라인 상태이면 이메일 알림이 전송된다.")
    void sendMessage_offline() {
        // Given
        ChatMessageDto messageDto = ChatMessageDto.builder()
                .roomId(100L)
                .senderNickname("Noah1")
                .content("테스트 메시지입니다.")
                .type(CHAT)
                .build();

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(chatRoom));
        when(clientRepository.findByNickName("Noah1")).thenReturn(Optional.of(sender));
        when(chatJoinRepository.findByChatRoomAndClientId(chatRoom, 1L)).thenReturn(Optional.of(senderJoin));
        when(chatJoinRepository.findByChatRoom(chatRoom)).thenReturn(List.of(senderJoin, receiverJoin));
        when(blockRepository.isUserBlocked(any(), any())).thenReturn(false);
        when(webSocketSessionService.isUserOnline(receiver.getNickName())).thenReturn(false);

        // When
        chatMessageService.sendMessage(messageDto);

        // Then
        verify(emailService, times(1)).sendNewMessageNotification(eq("noah2@gmail.com"), eq("Noah1"), contains("테스트"));
    }

    @Test
    @DisplayName("메시지 내용이 1000자를 초과할 수 없다.")
    void sendMessage_1000() {
        // Given
        String longMessage = "a".repeat(1001);
        ChatMessageDto messageDto = ChatMessageDto.builder()
                .roomId(100L)
                .senderNickname("Noah1")
                .content(longMessage)
                .type(CHAT)
                .build();

        // When & Then
        assertThatThrownBy(() -> chatMessageService.sendMessage(messageDto))
                .isInstanceOf((IllegalArgumentException.class))
                .hasMessageContaining("최대 1000자까지");
    }

    @Test
    @DisplayName("차단된 사용자와는 채팅을 할 수 없다.")
    void sendMessage_blockedUser() {
        // Given
        ChatMessageDto messageDto = ChatMessageDto.builder()
                .roomId(100L)
                .senderNickname("Noah1")
                .content("안녕")
                .type(CHAT)
                .build();

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(chatRoom));
        when(clientRepository.findByNickName("Noah1")).thenReturn(Optional.of(sender));
        when(chatJoinRepository.findByChatRoomAndClientId(chatRoom, sender.getId())).thenReturn(Optional.of(senderJoin));
        when(chatJoinRepository.findByChatRoom(chatRoom)).thenReturn(List.of(senderJoin, receiverJoin));
        when(blockRepository.isUserBlocked(sender, receiver)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> chatMessageService.sendMessage(messageDto))
                .isInstanceOf((IllegalArgumentException.class))
                .hasMessageContaining("차단된 사용자");
    }

    @Test
    @DisplayName("채팅방에 참여하지 않은 사용자와는 채팅을 할 수 없다.")
    void sendMessage_notParticipant() {
        // Given
        ChatMessageDto messageDto = ChatMessageDto.builder()
                .roomId(100L)
                .senderNickname("Noah1")
                .content("안녕")
                .type(CHAT)
                .build();

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(chatRoom));
        when(clientRepository.findByNickName("Noah1")).thenReturn(Optional.of(sender));
        when(chatJoinRepository.findByChatRoomAndClientId(chatRoom, sender.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatMessageService.sendMessage(messageDto))
                .isInstanceOf((IllegalArgumentException.class))
                .hasMessageContaining("채팅방에 참여한 사용자가 아닙니다.");
    }

    @Test
    @DisplayName("나간 채팅방에서는 메시지를 보낼 수 없다.")
    void sendMessage_alreadyExit() {
        // Given
        senderJoin.setExited(true);

        ChatMessageDto messageDto = ChatMessageDto.builder()
                .roomId(100L)
                .senderNickname("Noah1")
                .content("안녕")
                .type(CHAT)
                .build();

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(chatRoom));
        when(clientRepository.findByNickName("Noah1")).thenReturn(Optional.of(sender));
        when(chatJoinRepository.findByChatRoomAndClientId(chatRoom, 1L)).thenReturn(Optional.of(senderJoin));

        // When & Then
        assertThatThrownBy(() -> chatMessageService.sendMessage(messageDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메시지를 보낼 수 없습니다.");
    }

    @Test
    @DisplayName("채팅방을 나간 사용자는 메시지 내역을 조회할 수 없다.")
    void getMessages_exitedUser() {
        // Given
        senderJoin.setExited(true);

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(chatRoom));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(chatJoinRepository.findByChatRoomAndClientId(chatRoom, 1L)).thenReturn(Optional.of(senderJoin));

        // When & Then
        assertThatThrownBy(() -> chatMessageService.getMessages(100L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("조회할 수 없습니다.");
    }
}