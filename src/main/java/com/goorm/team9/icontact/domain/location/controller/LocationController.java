package com.goorm.team9.icontact.domain.location.controller;

import com.goorm.team9.icontact.domain.location.dto.request.DeleteRequestDto;
import com.goorm.team9.icontact.domain.location.dto.request.LocationRequestDto;
import com.goorm.team9.icontact.domain.location.dto.request.NearbyRequestDto;
import com.goorm.team9.icontact.domain.location.dto.request.RefreshRequestDto;
import com.goorm.team9.icontact.domain.location.dto.response.LocationResponseDto;
import com.goorm.team9.icontact.domain.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Map<String, Object>> saveLocation(@RequestBody LocationRequestDto request) {
        locationService.saveUserInformation(request.getId(), request.getLatitude(), request.getLongitude());

        Map<String, Object> response = Map.of(
                "message", "위치 데이터가 저장되었습니다.",
                "id", request.getId(),
                "latitude", request.getLatitude(),
                "longitude", request.getLongitude()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "위치 데이터 삭제",
            description = "참가자의 위치 데이터를 삭제합니다."
    )
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteLocation(@RequestBody DeleteRequestDto request) {
        locationService.deleteUserLocation(request.getId());

        Map<String, Object> response = Map.of(
                "message", "위치 데이터가 삭제되었습니다.",
                "id", request.getId()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "근처 참가자 조회",
            description = "참가자의 위치와 관심분야 일치도를 기반으로 반경 10m 이내의 참가자를 조회합니다. 직무와 경력 조건은 선택적으로 필터링에 사용됩니다."
    )
    @PostMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyUsers(@RequestBody NearbyRequestDto request) {
        List<LocationResponseDto> nearbyUsers = locationService.getNearbyUsers(request.getId(), request.getRole(), request.getCareer());

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
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshNearbyUsers(@RequestBody RefreshRequestDto request) {
        List<LocationResponseDto> nearbyUsers = locationService.refreshNearbyUsers(
                request.getId(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRole(),
                request.getCareer()
        );

        Map<String, Object> response = Map.of(
                "message", "주변 참가자 재조회가 완료되었습니다.",
                "data", nearbyUsers
        );

        return ResponseEntity.ok(response);
    }

}