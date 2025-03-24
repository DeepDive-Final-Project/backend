package com.goorm.team9.icontact.domain.chat.service;

import com.goorm.team9.icontact.domain.chat.dto.ChatRequestCountDto;
import com.goorm.team9.icontact.domain.chat.dto.ChatRequestDto;
import com.goorm.team9.icontact.domain.chat.dto.ChatRequestNotificationDto;
import com.goorm.team9.icontact.domain.chat.dto.ChatResponseDto;
import com.goorm.team9.icontact.domain.chat.entity.ChatRequest;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.chat.entity.RequestStatus;
import com.goorm.team9.icontact.domain.chat.repository.ChatRequestRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRoomRepository;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.goorm.team9.icontact.domain.chat.entity.ChatRoom.createChatRoom;

@Service
@RequiredArgsConstructor
public class ChatRequestService {

    private final ChatRequestRepository chatRequestRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ClientRepository clientRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final WebSocketSessionService webSocketSessionservice;
    private final EmailService emailService;

    @Transactional
    public ResponseEntity<ChatResponseDto> requestChat(ClientEntity sender, ClientEntity receiver) {
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findExistingChatRoom(sender.getNickName(), receiver.getNickName());

        if (existingChatRoom.isPresent()) {
            return ResponseEntity.ok(new ChatResponseDto(null, "이미 채팅방이 존재합니다.", null));
        }

        Optional<ChatRequest> existingRequest = chatRequestRepository.findPendingRequest(sender.getNickName(), receiver.getNickName());

        if (existingRequest.isPresent()) {
            return ResponseEntity.ok(new ChatResponseDto(existingRequest.get().getId(), "이미 채팅 요청을 보냈습니다.", null));
        }

        ChatRequest chatRequest = ChatRequest.create(sender, receiver);
        Long requestId = chatRequestRepository.save(chatRequest).getId();

        ChatRequestNotificationDto notification = new ChatRequestNotificationDto(
                sender.getNickName(),
                requestId,
                LocalDateTime.now()
        );

        String destination = "/queue/chat-request" + receiver.getNickName();
        simpMessagingTemplate.convertAndSend(destination, notification);

        if (!webSocketSessionservice.isUserOnline(receiver.getNickName())) {
            String receiverEmail = receiver.getEmail();
            emailService.sendChatRequestNotification(receiverEmail, sender.getNickName());
        }

        return ResponseEntity.ok(new ChatResponseDto(requestId, "채팅 요청이 정상적으로 전송되었습니다.", null));
    }

    @Transactional(readOnly = true)
    public Optional<ChatResponseDto> getChatRequestById(Long requestId) {
        Optional<ChatRequest> chatRequestOpt = chatRequestRepository.findById(requestId);
        if (chatRequestOpt.isEmpty()) return Optional.empty();

        ChatRequest chatRequest = chatRequestOpt.get();
        Long roomId = null;

        if (chatRequest.getStatus() == RequestStatus.ACCEPTED) {
            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findExistingChatRoom(
                    chatRequest.getSenderNickname(),
                    chatRequest.getReceiverNickname()
            );
            roomId = chatRoomOpt.map(ChatRoom::getRoomId).orElse(null);
        }

        return Optional.of(new ChatResponseDto(
                chatRequest.getId(),
                chatRequest.getStatus().toString(),
                roomId
        ));
    }

    @Transactional
    public Long acceptChatRequest(Long requestId) {
        ChatRequest chatRequest = chatRequestRepository.findByIdAndStatus(requestId, RequestStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청이 없거나 이미 처리되었습니다."));

        ClientEntity sender = clientRepository.findByNickName(chatRequest.getSenderNickname())
                        .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));
        ClientEntity receiver = clientRepository.findByNickName(chatRequest.getReceiverNickname())
                        .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

        chatRequest.accept();
        chatRequestRepository.save(chatRequest);

        ChatRoom chatRoom = createChatRoom(sender, receiver);
        chatRoomRepository.save(chatRoom);

        return chatRoom.getRoomId();
    }

    public void rejectChatRequest(Long requestId) {
        ChatRequest chatRequest = chatRequestRepository.findByIdAndStatus(requestId, RequestStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청이 없거나 이미 처리되었습니다."));

        chatRequest.reject();
        chatRequestRepository.save(chatRequest);
    }

    @Transactional(readOnly = true)
    public List<ChatRequestDto> getReceivedRequests(String receiverNickname, RequestStatus status) {
        ClientEntity receiver = clientRepository.findByNickName(receiverNickname)
                .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

        return chatRequestRepository.findByReceiverNicknameAndStatus(receiverNickname, status)
                .stream()
                .map(ChatRequestDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatRequestDto> getSentRequest(String senderNickname, RequestStatus status) {
        ClientEntity sender = clientRepository.findByNickName(senderNickname)
                .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));

        return chatRequestRepository.findBySenderNicknameAndStatus(senderNickname, status)
                .stream()
                .map(ChatRequestDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatRequestCountDto getRequestCounts(String nickname) {
        ClientEntity client = clientRepository.findByNickName(nickname)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        long receivedCount = chatRequestRepository.countReceivedRequests(nickname, RequestStatus.PENDING);
        long sentCount = chatRequestRepository.countSentRequests(nickname, RequestStatus.PENDING);

        return new ChatRequestCountDto(receivedCount, sentCount);
    }
}
