package com.goorm.team9.icontact.domain.location.controller;

import com.goorm.team9.icontact.domain.location.dto.LocationResponse;
import com.goorm.team9.icontact.domain.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Location API", description = "위치 관련 API")
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @Operation(summary = "위치 및 관심사 정보 저장", description = "참가자 위치 및 관심 분야를 저장하는 API입니다.")
    @PostMapping("/save")
    public ResponseEntity<String> saveLocation(
            @Parameter(description = "참가자 ID", required = true) @RequestParam Long id,
            @Parameter(description = "위도(latitude)", required = true) @RequestParam double latitude,
            @Parameter(description = "경도(longitude)", required = true) @RequestParam double longitude
    ) {
        locationService.saveUserInformation(id, latitude, longitude);

        String responseMessage = String.format(
                "위치 데이터가 저장되었습니다. (ID: %d, 위도: %.6f, 경도: %.6f)",
                id, latitude, longitude
        );

        return ResponseEntity.ok(responseMessage);
    }

    @Operation(summary = "근처 참가자 조회", description = "참가자 ID를 기반으로 반경 내 참가자를 조회하는 API입니다.")
    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyUsers(
            @Parameter(description = "참가자 ID", required = true) @RequestParam Long id
    ) {
        List<LocationResponse> nearbyUsers = locationService.getNearbyUsers(id);

        Map<String, Object> response = Map.of(
                "message", "근처 참가자 조회 완료",
                "data", nearbyUsers
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshNearbyUsers(
            @Parameter(description = "참가자 ID", required = true) @RequestParam Long id,
            @Parameter(description = "현재 위도(latitude)", required = true) @RequestParam double latitude,
            @Parameter(description = "현재 경도(longitude)", required = true) @RequestParam double longitude
    ) {
        List<LocationResponse> nearbyUsers = locationService.refreshNearbyUsers(id, latitude, longitude);

        Map<String, Object> response = Map.of(
                "message", "주변 참가자 새로고침 완료",
                "data", nearbyUsers
        );

        return ResponseEntity.ok(response);
    }
}
