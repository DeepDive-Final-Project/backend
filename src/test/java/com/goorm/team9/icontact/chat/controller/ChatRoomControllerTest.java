package com.goorm.team9.icontact.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.team9.icontact.domain.chat.controller.ChatRoomController;
import com.goorm.team9.icontact.domain.chat.dto.ChatRoomRequest;
import com.goorm.team9.icontact.domain.chat.dto.ChatRoomResponse;
import com.goorm.team9.icontact.domain.chat.service.ChatRoomService;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatRoomController.class)
@WithMockUser
public class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatRoomService chatRoomService;

    @MockitoBean
    private ClientRepository clientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("채팅방 생성을 성공한다.")
    void 채팅방_생성_성공_테스트() throws Exception {
        ChatRoomRequest request = new ChatRoomRequest("Noah1", "Noah2");

        ClientEntity sender = new ClientEntity();
        sender.setNickName("Noah1");

        ClientEntity receiver = new ClientEntity();
        receiver.setNickName("Noah2");

        when(clientRepository.findByNickName("Noah1")).thenReturn(Optional.of(sender));
        when(clientRepository.findByNickName("Noah2")).thenReturn(Optional.of(receiver));
        when(chatRoomService.createChatRoom(sender, receiver)).thenReturn(101L);

        mockMvc.perform(post("/api/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomId").value(101L))
                .andExpect(jsonPath("$.data.senderNickname").value("Noah1"))
                .andExpect(jsonPath("$.data.receiverNickname").value("Noah2"))
                .andExpect(jsonPath("$.message").value("채팅방이 생성되었습니다."));
    }

    @Test
    @DisplayName("채팅방 퇴장 시 퇴장 메시지를 반환한다.")
    void 채팅방_퇴장_테스트() throws Exception {
        ClientEntity client = new ClientEntity();
        client.setNickName("Noah1");

        when(clientRepository.findById(2L)).thenReturn(Optional.of(client));

        mockMvc.perform(post("/api/chat/exit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\": 1, \"clientId\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Noah1님이 퇴장했습니다."));
    }

    @Test
    @DisplayName("채팅방 입장 시 last_read_at 업데이트된다.")
    void 채팅방_입장_테스트() throws Exception {
        mockMvc.perform(post("/api/chat/enter")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\": 1, \"clientId\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("last_read_at이 업데이트되었습니다."));
    }

    @Test
    @DisplayName("최신 메시지순으로 채팅방을 조회한다.")
    void 최신순_채팅방_조회() throws Exception {
        ClientEntity client = new ClientEntity();
        client.setNickName("Noah1");

        ChatRoomResponse response = new ChatRoomResponse(
                1L,
                List.of("Noah1", "Noah2"),
                "안녕하세요",
                LocalDateTime.now(),
                2L
        );

        when(clientRepository.findByNickName("Noah1")).thenReturn(Optional.of(client));
        when(chatRoomService.getLatestChatRooms(client)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/chat/latest")
                        .param("nickname", "Noah1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomId").value(1L))
                .andExpect(jsonPath("$[0].participants[0]").value("Noah1"))
                .andExpect(jsonPath("$[0].participants[1]").value("Noah2"))
                .andExpect(jsonPath("$[0].lastMessage").value("안녕하세요"))
                .andExpect(jsonPath("$[0].unreadCount").value(2));
    }

    @Test
    @DisplayName("읽지 않은 메시지가 있는 채팅방 목록을 조회한다.")
    void 읽지않은채팅방_조회() throws Exception {
        ClientEntity client = new ClientEntity();
        client.setNickName("Noah1");

        ChatRoomResponse response = new ChatRoomResponse(
                2L,
                List.of("Noah1", "Noah2"),
                "안녕하세요",
                LocalDateTime.now(),
                2L
        );
    }
}