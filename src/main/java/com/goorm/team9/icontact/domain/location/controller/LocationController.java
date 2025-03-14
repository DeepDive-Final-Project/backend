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
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @Operation(summary = "위치 데이터 저장", description = "사용자의 위치 및 관심 분야를 저장하는 API입니다.")
    @PostMapping("/save")
    public ResponseEntity<String> saveLocation(
            @Parameter(description = "사용자 ID", required = true) @RequestParam Long id,
            @Parameter(description = "위도(latitude)", required = true) @RequestParam double latitude,
            @Parameter(description = "경도(longitude)", required = true) @RequestParam double longitude,
            @Parameter(description = "사용자의 관심 분야", required = true) @RequestParam String interest
    ) {
        boolean isUpdated = locationService.saveLocation(id, latitude, longitude, interest);
        return ResponseEntity.ok(isUpdated ? "위치 데이터가 갱신되었습니다." : "위치 변화가 없어 위치 데이터가 유지되었습니다.");
    }

    @Operation(summary = "근처 참가자 조회", description = "사용자의 현재 위치와 관심 분야를 기반으로 반경 내 참가자를 조회하는 API입니다.")
    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyUsers(
            @Parameter(description = "현재 위도(latitude)", required = true) @RequestParam double latitude,
            @Parameter(description = "현재 경도(longitude)", required = true) @RequestParam double longitude,
            @Parameter(description = "사용자의 관심 분야", required = true) @RequestParam String interest
    ) {
        List<LocationResponse> nearbyUsers = locationService.getNearbyUsers(latitude, longitude, interest);

        Map<String, Object> response = Map.of(
                "message", "근처 참가자 조회 완료",
                "data", nearbyUsers
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "새로고침(주변 참가자 재조회)", description = "사용자의 위치를 확인하고, 최신 참가자 목록을 조회하는 API입니다.")
    @GetMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshNearbyUsers(
            @Parameter(description = "사용자의 ID", required = true) @RequestParam Long id,
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
