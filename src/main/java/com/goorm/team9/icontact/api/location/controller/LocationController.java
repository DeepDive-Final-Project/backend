package com.goorm.team9.icontact.api.location.controller;

import com.goorm.team9.icontact.api.location.dto.LocationRequest;
import com.goorm.team9.icontact.api.location.dto.LocationResponse;
import com.goorm.team9.icontact.api.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
            @Parameter(description = "사용자 ID", required = true) @RequestParam String userId,
            @Parameter(description = "위도(latitude)", required = true) @RequestParam double latitude,
            @Parameter(description = "경도(longitude)", required = true) @RequestParam double longitude,
            @Parameter(description = "사용자의 관심 분야", required = true) @RequestParam String interest
    ) {
        LocationRequest request = new LocationRequest(userId, latitude, longitude, interest);
        locationService.saveLocation(request);
        return ResponseEntity.ok("위치_데이터 저장 완료");
    }

    @Operation(summary = "근처 참가자 조회", description = "사용자의 현재 위치와 관심 분야를 기반으로 반경 내 참가자를 조회하는 API입니다.")
    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyUsers(
            @Parameter(description = "현재 위도(latitude)", required = true) @RequestParam double latitude,
            @Parameter(description = "현재 경도(longitude)", required = true) @RequestParam double longitude,
            @Parameter(description = "사용자의 관심 분야", required = true) @RequestParam String interest
    ) {
        List<LocationResponse> nearbyUsers = locationService.getNearbyUsers(latitude, longitude, interest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "근처 참가자 조회 및 관심분야 추출 완료");
        response.put("data", nearbyUsers);

        return ResponseEntity.ok(response);
    }
}
