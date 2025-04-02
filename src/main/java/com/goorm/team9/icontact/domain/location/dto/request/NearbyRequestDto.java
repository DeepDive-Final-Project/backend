package com.goorm.team9.icontact.domain.location.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "근처 참가자 조회 요청 정보")
public class NearbyRequestDto {

    @Schema(description = "참가자 ID", example = "11")
    private Long id;

    @Schema(description = "직무 (예: 개발자)", example = "개발자", required = false)
    private String role;

    @Schema(description = "경력 (예: 주니어)", example = "주니어", required = false)
    private String career;

}
