package com.goorm.team9.icontact.domain.chat.dto;

import com.goorm.team9.icontact.domain.chat.entity.ChatRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequestDto {

    private Long id;

    @Schema(example = "Noah1", description = "채팅을 요청하는 사용자 닉네임")
    private String senderNickname;

    @Schema(example = "Noah2", description = "채팅 요청을 받는 사용자 닉네임")
    private String receiverNickname;

    private String status;

    public static ChatRequestDto fromEntity(ChatRequest chatRequest) {
        return ChatRequestDto.builder()
                .id(chatRequest.getId())
                .senderNickname(chatRequest.getSenderNickname().getNickName())
                .receiverNickname(chatRequest.getReceiverNickname().getNickName())
                .status(chatRequest.getStatus().toString())
                .build();
    }
}
