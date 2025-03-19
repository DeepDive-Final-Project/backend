package com.goorm.team9.icontact.domain.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageCreateRequest;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageUpdateRequest;
import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDTO;
import com.goorm.team9.icontact.domain.client.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/client/profile")
@RequiredArgsConstructor
@Tag(name = "MyPage API", description = "마이페이지 생성 및 수정 API")
@Slf4j
public class ClientController {

    private final ClientService clientService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "마이페이지 생성 API", description = "사용자 정보를 입력하여 마이페이지를 생성합니다.")
    public ResponseEntity<?> createMyPage(
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "userData") String userData
    ) {
        try {
            log.info("Received userData: {}", userData);  // JSON 데이터 로깅
            MyPageCreateRequest request = objectMapper.readValue(userData.trim(), MyPageCreateRequest.class);
            return ResponseEntity.ok(clientService.createMyPage(request, profileImage));
        } catch (Exception e) {
            log.error("JSON Parsing Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid JSON format: " + e.getMessage());
        }
    }

    @PatchMapping(value = "/update/{clientId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "마이페이지 수정 API", description = "사용자 정보를 수정합니다.")
    public ResponseEntity<?> updateUser(
            @PathVariable Long clientId,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "userData") String userData
    ) {
        try {
            log.info("Received userData for update: {}", userData);  // JSON 데이터 로깅
            MyPageUpdateRequest request = objectMapper.readValue(userData.trim(), MyPageUpdateRequest.class);
            return ResponseEntity.ok(clientService.updateUser(clientId, request, profileImage));
        } catch (Exception e) {
            log.error("JSON Parsing Error during update: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid JSON format: " + e.getMessage());
        }
    }

    @GetMapping("/{clientId}")
    @Operation(summary = "사용자 정보 출력 API", description = "다른 사용자의 정보를 확인합니다.")
    public ResponseEntity<ClientResponseDTO> getUserById(
            @PathVariable Long clientId
    ) {
        return ResponseEntity.ok(clientService.getUserById(clientId));
    }

    @GetMapping("/all")
    @Operation(summary = "전체 사용자 조회 API", description = "삭제되지 않은 모든 사용자의 정보를 반환합니다.")
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

}
