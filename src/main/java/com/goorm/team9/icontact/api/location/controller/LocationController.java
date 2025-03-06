package com.goorm.team9.icontact.api.location.controller;

import com.goorm.team9.icontact.api.location.dto.LocationRequest;
import com.goorm.team9.icontact.api.location.dto.LocationResponse;
import com.goorm.team9.icontact.api.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<String> saveLocation(@RequestBody LocationRequest request) {
        locationService.saveLocation(request);
        return ResponseEntity.ok("위치_데이터 저장 완료");
    }

    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyUsers(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        List<LocationResponse> nearbyUsers = locationService.getNearbyUsers(latitude, longitude);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "근처 참가자 조회 완료");
        response.put("data", nearbyUsers);

        return ResponseEntity.ok(response);
    }
}
