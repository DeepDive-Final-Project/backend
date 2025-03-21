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

    @Operation(
            summary = "위치 데이터 저장",
            description = "참가자의 위치 데이터를 저장합니다. 기존 위치가 존재하는 경우 갱신되며, 위치 데이터는 30초 동안 유지됩니다."
    )
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

    @Operation(
            summary = "위치 데이터 삭제",
            description = "참가자의 위치 데이터 삭제합니다."
    )
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteLocation(
            @Parameter(description = "참가자 ID", required = true) @RequestParam Long id
    ) {
        locationService.deleteUserLocation(id);

        Map<String, Object> response = Map.of(
                "message", "위치 데이터가 삭제되었습니다.",
                "id", id
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "근처 참가자 조회",
            description = "참가자의 위치와 관심분야 일치도를 기반으로 반경 10m 이내의 참가자를 재조회합니다. 직무와 경력 조건은 선택적으로 필터링에 사용됩니다."
    )
    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyUsers(
            @Parameter(description = "참가자 ID", required = true) @RequestParam Long id,
            @Parameter(description = "직무(Role) - 예: 개발자", required = false) @RequestParam(required = false) String role,
            @Parameter(description = "경력(Career) - 예: 주니어", required = false) @RequestParam(required = false) String career
    ) {
        List<LocationResponse> nearbyUsers = locationService.getNearbyUsers(id, role, career);

        Map<String, Object> response = Map.of(
                "message", "근처 참가자 조회가 완료되었습니다.",
                "data", nearbyUsers
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "근처 참가자 재조회",
            description = "참가자의 위치와 관심분야 일치도를 기반으로 반경 10m 이내의 참가자를 재조회합니다. 직무와 경력 조건은 선택적으로 필터링에 사용됩니다."
    )
    @GetMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshNearbyUsers(
            @Parameter(description = "참가자 ID", required = true) @RequestParam Long id,
            @Parameter(description = "현재 위도(latitude)", required = true) @RequestParam double latitude,
            @Parameter(description = "현재 경도(longitude)", required = true) @RequestParam double longitude,
            @Parameter(description = "직무(Role) - 예: 개발자", required = false) @RequestParam(required = false) String role,
            @Parameter(description = "경력(Career) - 예: 주니어", required = false) @RequestParam(required = false) String career
    ) {
        List<LocationResponse> nearbyUsers = locationService.refreshNearbyUsers(id, latitude, longitude, role, career);

        Map<String, Object> response = Map.of(
                "message", "주변 참가자 재조회가 완료되었습니다.",
                "data", nearbyUsers
        );

        return ResponseEntity.ok(response);
    }
}