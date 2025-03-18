package com.goorm.team9.icontact.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRequestDto {

    @Schema(example = "Noah1", description = "채팅을 요청하는 사용자 닉네임")
    private String senderNickname;

    @Schema(example = "Noah2", description = "채팅 요청을 받는 사용자 닉네임")
    private String receiverNickname;
}
