package com.goorm.team9.icontact.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.team9.icontact.domain.chat.controller.ChatRequestController;
import com.goorm.team9.icontact.domain.chat.dto.request.ChatRequestCountDto;
import com.goorm.team9.icontact.domain.chat.dto.request.ChatRequestDto;
import com.goorm.team9.icontact.domain.chat.dto.request.ChatResponseDto;
import com.goorm.team9.icontact.domain.chat.entity.RequestStatus;
import com.goorm.team9.icontact.domain.chat.service.ChatRequestService;
import com.goorm.team9.icontact.domain.chat.service.ChatRoomService;
import com.goorm.team9.icontact.domain.client.entity.ClientEntity;
import com.goorm.team9.icontact.domain.client.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(controllers = ChatRequestController.class)
class ChatRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatRequestService chatRequestService;

    @MockitoBean
    private ChatRoomService chatRoomService;

    @MockitoBean
    private ClientRepository clientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("채팅 요청 성공 시 DB에 저장하고 응답을 확인한다.")
    void 채팅요청_성공_테스트() throws Exception {
        ChatRequestDto requestDto = ChatRequestDto.builder()
                .senderNickname("Noah1")
                .receiverNickname("Noah2")
                .build();

        ChatResponseDto responseDto = new ChatResponseDto(
                100L,
                "채팅 요청이 정상적으로 전송되었습니다.",
                null
        );

        ClientEntity sender = new ClientEntity();
        sender.setNickName("Noah1");
        ClientEntity receiver = new ClientEntity();
        receiver.setNickName("Noah2");

        when(clientRepository.findByNickName("Noah1")).thenReturn(Optional.of(sender));
        when(clientRepository.findByNickName("Noah2")).thenReturn(Optional.of(receiver));
        when(chatRequestService.requestChat(any(), any())).thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/api/chat/request")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(100L))
                .andExpect(jsonPath("$.message").value("채팅 요청이 정상적으로 전송되었습니다."));
    }

    @Test
    @DisplayName("채팅 요청 승인 시 채팅방이 생성되고 roomId를 반환한다.")
    void 채팅요청_승인_테스트() throws Exception {
        when(chatRequestService.acceptChatRequest(1L))
                .thenReturn(999L);

        mockMvc.perform(patch("/api/chat/request/accept/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("채팅 요청이 수락되었습니다."))
                .andExpect(jsonPath("$.roomId").value(999L));
    }

    @Test
    @DisplayName("채팅 요청 거절 시 상태가 변경되고 거절 메시지를 반환한다.")
    void 채팅요청_거절_테스트() throws Exception {
        mockMvc.perform(patch("/api/chat/request/reject/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("채팅 요청이 거절되었습니다."));

    }

    @Test
    @DisplayName("받은 채팅 요청 목록을 조회한다.")
    void 받은요청_조회_테스트() throws Exception {
        when(chatRequestService.getReceivedRequests("Noah2", RequestStatus.PENDING))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/chat/request/received")
                .param("receiverNickname", "Noah2")
                .param("status", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("보낸 채팅 요청 목록을 조회한다.")
    void 보낸요청_조회_테스트() throws Exception {
        when(chatRequestService.getSentRequest("Noah1", RequestStatus.PENDING))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/chat/request/sent")
                .param("senderNickname", "Noah1")
                .param("status", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("채팅 요청 개수를 조회한다.")
    void 채팅요청개수_조회_테스트() throws Exception {
        when(chatRequestService.getRequestCounts("Noah1"))
                .thenReturn(new ChatRequestCountDto(5L, 3L));

        mockMvc.perform(get("/api/chat/request/count")
                .param("nickname", "Noah1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(3L))
                .andExpect(jsonPath("$.receivedCount").value(5L));

    }
}