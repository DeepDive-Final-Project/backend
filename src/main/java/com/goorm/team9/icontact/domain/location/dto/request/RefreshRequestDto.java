package com.goorm.team9.icontact.domain.location.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "위치 새로고침 요청 정보")
public class RefreshRequestDto {

    @Schema(description = "참가자 ID", example = "11")
    private Long id;

    @Schema(description = "현재 위도", example = "37.402056")
    private double latitude;

    @Schema(description = "현재 경도", example = "127.108212")
    private double longitude;

    @Schema(description = "직무 (예: 개발자)", example = "개발자", required = false)
    private String role;

    @Schema(description = "경력 (예: 주니어)", example = "주니어", required = false)
    private String career;

}
