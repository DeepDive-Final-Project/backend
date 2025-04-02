package com.goorm.team9.icontact.domain.location.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "위치 삭제 요청 정보")
public class DeleteRequestDto {

    @Schema(description = "참가자 ID", example = "11")
    private Long id;

}
