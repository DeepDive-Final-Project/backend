package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.domain.chat.entity.ChatJoin;
import com.goorm.team9.icontact.domain.chat.entity.ChatRoom;
import com.goorm.team9.icontact.domain.chat.repository.ChatJoinRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatMessageRepository;
import com.goorm.team9.icontact.domain.chat.repository.ChatRoomRepository;
import com.goorm.team9.icontact.domain.chat.service.ChatRoomService;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import com.goorm.team9.icontact.domain.client.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ChatJoinRepository chatJoinRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ChatRoomService chatRoomService;

    private ClientEntity sender;
    private ClientEntity receiver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sender = ClientEntity.builder().nickName("Noah1").build();
        receiver = ClientEntity.builder().nickName("Noah2").build();
    }

    @Test
    @DisplayName("기존 채팅을 나눈 사용자와의 채팅일 경우 기존 채팅방 ID를 반환한다.")
    void createOrGetRoomId_existingRoom() {
        // Given
        ChatRoom existingRoom = ChatRoom.createChatRoom(sender, receiver);
        existingRoom.setRoomId(42L);

        when(chatRoomRepository.findBySenderNicknameOrReceiverNickname(sender.getNickName()))
                .thenReturn(List.of(existingRoom));

        // When
        Long roomId = chatRoomService.createOrGetRoomId(sender, receiver);

        // Then
        assertThat(roomId).isEqualTo(42L);
    }

    @Test
    @DisplayName("새로운 사용자와의 채팅일 경우 새로운 채팅방을 생성한다.")
    void createOrGetRoomId_newRoom() {
        // Given
        when(chatRoomRepository.findBySenderNicknameOrReceiverNickname(sender.getNickName()))
                .thenReturn(List.of());

        when(chatRoomRepository.findExistingChatRoom(sender.getNickName(), receiver.getNickName()))
                .thenReturn(Optional.empty());

        when(chatRoomRepository.save(any(ChatRoom.class)))
                .thenAnswer(invocation -> {
                    ChatRoom savedRoom = invocation.getArgument(0);
                    savedRoom.setRoomId(100L);
                    return savedRoom;
                });

        when(chatJoinRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Long roomId = chatRoomService.createOrGetRoomId(sender, receiver);

        // Then
        assertThat(roomId).isEqualTo(100L);
    }

    @Test
    @DisplayName("이미 존재하는 채팅방이 있을 경우 메시지를 반환한다.")
    void createChatRoom_alreadyExists() {
        when(chatRoomRepository.findExistingChatRoom(sender.getNickName(), receiver.getNickName()))
                .thenReturn(Optional.of(new ChatRoom()));

        assertThatThrownBy(() -> chatRoomService.createChatRoom(sender, receiver))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 채팅방이 존재합니다.");
    }

    @Test
    @DisplayName("사용자가 채팅방에서 퇴장하고, 마지막 사용자일 경우 채팅방이 삭제된다.")
    void exitChatRoom_success_and_deleteChatRoom() {
        // Given
        Long roomId = 1L;
        Long clientId = 1L;

        ChatRoom chatRoom = ChatRoom.createChatRoom(sender, receiver);
        chatRoom.setRoomId(roomId);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(sender));

        ChatJoin chatJoin = new ChatJoin();
        chatJoin.setChatRoom(chatRoom);
        chatJoin.setClient(sender);
        chatJoin.setExited(false);

        when(chatJoinRepository.findByChatRoomAndClientId(chatRoom, clientId)).thenReturn(Optional.of(chatJoin));
        when(chatJoinRepository.countByChatRoomAndExitedFalse(chatRoom)).thenReturn(0L);
        when(chatJoinRepository.findByChatRoom(chatRoom)).thenReturn(List.of(chatJoin));

        // When
        chatRoomService.exitChatRoom(roomId, clientId);

        // Then
        assertThat(chatJoin.isExited()).isTrue();
    }

    @Test
    @DisplayName("채팅방 입장 시 사용자의 마지막 읽은 시간이 업데이트된다.")
    void updateLastReadAt_success() {
        // Given
        Long roomId = 1L;
        Long clientId = 1L;

        ChatRoom chatRoom = ChatRoom.createChatRoom(sender, receiver);
        chatRoom.setRoomId(roomId);

        ChatJoin chatJoin = new ChatJoin();
        chatJoin.setChatRoom(chatRoom);
        chatJoin.setClient(sender);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(chatJoinRepository.findByChatRoomAndClientId(chatRoom, clientId)).thenReturn(Optional.of(chatJoin));

        // When
        chatRoomService.updateLastReadAt(roomId, clientId);

        // Then
        assertThat(chatJoin.getLastReadAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자의 채팅방 내 읽지 않은 메시지 개수를 반환한다.")
    void countUnreadMessages_success() {
        // Given
        Long roomId = 1L;
        Long clientId = 1L;

        when(chatMessageRepository.countUnreadMessages(roomId, clientId)).thenReturn(5L);

        // When
        long unreadCount = chatRoomService.countUnreadMessages(roomId, clientId);

        // Then
        assertThat(unreadCount).isEqualTo(5L);
    }
}
