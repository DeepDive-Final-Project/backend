package com.goorm.team9.icontact.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatResponseDto {

    @Schema(example = "100", description = "채팅 요청 ID")
    private final Long requestId;

    @Schema(example = "채팅이 요청되었습니다.", description = "응답 메시지")
    private final String message;

    @Schema(example = "123", description = "채팅방 ID")
    private final Long roomId;
}
