package com.goorm.team9.icontact.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.team9.icontact.domain.chat.controller.ChatMessageController;
import com.goorm.team9.icontact.domain.chat.dto.ChatMessageDto;
import com.goorm.team9.icontact.domain.chat.service.ChatMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatMessageController.class)
@WithMockUser
public class ChatMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatMessageService chatMessageService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("채팅 메시지 목록을 조회한다.")
    void 채팅_메시지_목록_조회_테스트() throws Exception {
        Long roomId = 1L;
        Long clientId = 2L;

        ChatMessageDto message1 = ChatMessageDto.builder()
                .roomId(roomId)
                .senderNickname("Noah1")
                .content("안녕하세요")
                .build();

        ChatMessageDto message2 = ChatMessageDto.builder()
                .roomId(roomId)
                .senderNickname("Noah2")
                .content("반가워요")
                .build();

        when(chatMessageService.getMessagesByRoomId(roomId, clientId))
                .thenReturn(List.of(message1, message2));

        mockMvc.perform(get("/api/messages/{roomId}", roomId)
                        .param("clientId", String.valueOf(clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderNickname").value("Noah1"))
                .andExpect(jsonPath("$[0].content").value("안녕하세요"))
                .andExpect(jsonPath("$[1].senderNickname").value("Noah2"))
                .andExpect(jsonPath("$[1].content").value("반가워요"));
    }

    @Test
    @DisplayName("채팅 메시지 읽음 처리한다.")
    void 채팅_메시지_읽음처리() throws Exception {
        Long roomId = 1L;
        Long clientId = 2L;

        doNothing().when(chatMessageService).markMessagesAsRead(roomId, clientId);

        mockMvc.perform(patch("/api/messages/{roomId}/read", roomId)
                        .with(csrf())
                        .param("clientId", String.valueOf(clientId)))
                .andExpect(status().isNoContent());
    }
}