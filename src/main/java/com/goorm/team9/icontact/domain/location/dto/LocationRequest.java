package com.goorm.team9.icontact.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "참가자의 위치 요청 정보")
public class LocationRequest {

    @Schema(description = "참가자 ID", example = "11")
    private Long id;

    @Schema(description = "위도 (latitude)", example = "37.402056")
    private double latitude;

    @Schema(description = "경도 (longitude)", example = "127.108212")
    private double longitude;
}
