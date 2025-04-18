package com.goorm.team9.icontact.domain.block.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UnblockRequesetDto {

    @Schema(example = "Noah1", description = "차단 해제를 요청하는 사용자 닉네임")
    private String blockerNickname;

    @Schema(example = "Noah2", description = "차단 해제될 사용자 닉네임")
    private String blockedNickname;

}
