package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.domain.chat.dto.request.ChatRequestDto;
import com.goorm.team9.icontact.domain.chat.dto.request.ChatResponseDto;
import com.goorm.team9.icontact.domain.chat.entity.ChatRequest;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.chat.entity.RequestStatus;
import com.goorm.team9.icontact.domain.chat.repository.ChatJoinRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRequestRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRoomRepository;
import com.goorm.team9.icontact.domain.chat.service.ChatRequestService;
import com.goorm.team9.icontact.domain.chat.service.EmailService;
import com.goorm.team9.icontact.domain.chat.service.WebSocketSessionService;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatRequestServiceTest {

    @Mock
    private ChatRequestRepository chatRequestRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ChatJoinRepository chatJoinRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private WebSocketSessionService webSocketSessionService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ChatRequestService chatRequestService;

    private ClientEntity sender;
    private ClientEntity receiver;

    @BeforeEach
    public void setUp() {
        sender = ClientEntity.builder().nickName("Noah1").email("noah1@email.com").provider("kakao").build();
        receiver = ClientEntity.builder().nickName("Noah2").email("noah2@email.com").provider("kakao").build();
    }

    @Test
    @DisplayName("새로운 채팅 요청이 생성된다.")
    void requestChat_success() {
        // Given
        when(chatRoomRepository.findExistingChatRoom(anyString(), anyString())).thenReturn(Optional.empty());
        when(chatRequestRepository.findPendingRequest(anyString(), anyString())).thenReturn(Optional.empty());

        when(chatRequestRepository.save(any(ChatRequest.class))).thenAnswer(invocation -> {
            ChatRequest request = invocation.getArgument(0);
            request.setId(1L);
            return request;
        });

        when(webSocketSessionService.isUserOnline(receiver.getNickName())).thenReturn(true);

        // When
        ResponseEntity<ChatResponseDto> response = chatRequestService.requestChat(sender, receiver);

        // Then
        assertThat(response.getBody().getRequestId()).isEqualTo(1L);
        assertThat(response.getBody().getMessage()).isEqualTo("채팅 요청이 정상적으로 전송되었습니다.");
    }

    @Test
    @DisplayName("이미 존재하는 채팅방이 있는 경우")
    void requestChat_chatRoomAlreadyExists() {
        // Given
        when(chatRoomRepository.findExistingChatRoom(anyString(), anyString()))
                .thenReturn(Optional.of(mock(ChatRoom.class)));

        // When
        ResponseEntity<ChatResponseDto> response = chatRequestService.requestChat(sender, receiver);

        // Then
        assertThat(response.getBody().getMessage()).isEqualTo("이미 채팅방이 존재합니다.");
    }

    @Test
    @DisplayName("이미 보낸 채팅 요청이 있는 경우")
    void requestChat_alreadySentRequest() {
        // Given
        when(chatRoomRepository.findExistingChatRoom(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(chatRequestRepository.findPendingRequest(anyString(), anyString()))
                .thenReturn(Optional.of(mock(ChatRequest.class)));

        // When
        ResponseEntity<ChatResponseDto> response = chatRequestService.requestChat(sender, receiver);

        // Then
        assertThat(response.getBody().getMessage()).isEqualTo("이미 채팅 요청을 보냈습니다.");
    }

    @Test
    @DisplayName("수신자가 오프라인 상태일 경우 이메일 알림 전송한다.")
    void requestChat_offline_emailSent() {
        // Given
        when(chatRoomRepository.findExistingChatRoom(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(chatRequestRepository.findPendingRequest(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(chatRequestRepository.save(any()))
                .thenReturn(ChatRequest.create(sender, receiver));
        when(webSocketSessionService.isUserOnline(receiver.getNickName())).thenReturn(false);

        // When
        chatRequestService.requestChat(sender, receiver);

        // Then
        verify(emailService).sendChatRequestNotification(receiver.getEmail(), sender.getNickName());
    }

    @Test
    @DisplayName("채팅 요청 승인 시 채팅방이 생성된다.")
    void acceptChatRequest_success() {
        // Given
        ChatRequest request = ChatRequest.create(sender, receiver);
        request.setId(1L);

        when(chatRequestRepository.findByIdAndStatus(eq(1L), eq(RequestStatus.PENDING)))
                .thenReturn(Optional.of(request));
        when(clientRepository.findByNickName(sender.getNickName()))
                .thenReturn(Optional.of(sender));
        when(clientRepository.findByNickName(receiver.getNickName()))
                .thenReturn(Optional.of(receiver));
        when(chatRoomRepository.save(any())).thenAnswer(invocation -> {
            ChatRoom savedRoom = invocation.getArgument(0);
            savedRoom.setRoomId(100L);
            return savedRoom;
        });

        // When
        Long roomId = chatRequestService.acceptChatRequest(1L);

        // Then
        assertThat(roomId).isEqualTo(100L);
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅 요청 거절 시 상태가 REJECTED로 변경된다.")
    void rejectChatRequest_success() {
        // Given
        ChatRequest request = ChatRequest.create(sender, receiver);
        request.setId(2L);

        when(chatRequestRepository.findByIdAndStatus(eq(2L), eq(RequestStatus.PENDING)))
                .thenReturn(Optional.of(request));

        // When
        chatRequestService.rejectChatRequest(2L);

        // Then
        verify(chatRequestRepository).save(any(ChatRequest.class));
    }

    @Test
    @DisplayName("닉네임 기준으로 받은 요청 목록 반환한다.")
    void getReceivedRequests_success() {
        // Given
        when(chatRequestRepository.findByReceiverNicknameAndStatus("Noah2", RequestStatus.PENDING))
                .thenReturn(List.of(ChatRequest.create(sender, receiver)));

        // When
        List<ChatRequestDto> result = chatRequestService.getReceivedRequests("Noah2", RequestStatus.PENDING);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("닉네임 기준으로 보낸 요청 목록 반환한다.")
    void getSentRequests_success() {
        // Given
        when(chatRequestRepository.findBySenderNicknameAndStatus("Noah1", RequestStatus.PENDING))
                .thenReturn(List.of(ChatRequest.create(sender, receiver)));

        // When
        List<ChatRequestDto> result = chatRequestService.getSentRequest("Noah1", RequestStatus.PENDING);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 채팅 요청 조회 시 빈 값을 반환한다.")
    void getChatRequestById_NotFound() {
        // Given
        when(chatRequestRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<ChatResponseDto> result = chatRequestService.getChatRequestById(1L);

        // Then
        assertThat(result).isEmpty();
    }
}