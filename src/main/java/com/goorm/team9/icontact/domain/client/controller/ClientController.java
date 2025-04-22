package com.goorm.team9.icontact.domain.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goorm.team9.icontact.domain.client.dto.request.*;
import com.goorm.team9.icontact.domain.client.dto.response.ClientProfileImageDto;
import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDto;
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
import java.util.Map;

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
            log.info("Received userData: {}", userData);
            MyPageCreateRequestDto request = objectMapper.readValue(userData.trim(), MyPageCreateRequestDto.class);
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
            MyPageUpdateRequestDto request = objectMapper.readValue(userData.trim(), MyPageUpdateRequestDto.class);
            return ResponseEntity.ok(clientService.updateUser(clientId, request, profileImage));
        } catch (Exception e) {
            log.error("JSON Parsing Error during update: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid JSON format: " + e.getMessage());
        }
    }

    @GetMapping("/{clientId}")
    @Operation(summary = "사용자 정보 출력 API", description = "다른 사용자의 정보를 확인합니다.")
    public ResponseEntity<ClientResponseDto> getUserById(
            @PathVariable("clientId") Long clientId
    ) {
        return ResponseEntity.ok(clientService.getUserById(clientId));
    }

    @GetMapping("/all")
    @Operation(summary = "전체 사용자 조회 API", description = "삭제되지 않은 모든 사용자의 정보를 반환합니다.")
    public ResponseEntity<List<ClientResponseDto>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/profile-images")
    @Operation(summary = "사용자 프로필 이미지 조회 API", description = "최대 10명의 사용자 ID를 입력받아 각자의 프로필 이미지를 반환합니다.")
    public ResponseEntity<?> getProfileImages(
            @RequestParam(required = false) List<Long> clientIds
    ) {
        if (clientIds == null || clientIds.size() > 10) {
            return ResponseEntity.badRequest().body("clientId는 최대 10개까지 입력 가능합니다.");
        }

        List<ClientProfileImageDto> images = clientService.getProfileImages(clientIds);
        return ResponseEntity.ok(images);
    }

    @PatchMapping("/nickname/{clientId}")
    @Operation(summary = "닉네임 수정 API", description = "사용자의 닉네임을 수정합니다.")
    public ResponseEntity<Map<String, String>> updateNickname(
            @PathVariable("clientId") Long clientId,
            @RequestBody UpdateNicknameRequestDto request
    ) {
        clientService.updateNickname(clientId, request.getNickName());
        return ResponseEntity.ok(Map.of("message", "닉네임 변경 성공!"));
    }


    @PatchMapping("/email/{clientId}")
    @Operation(summary = "이메일 수정 API", description = "사용자의 이메일을 수정합니다.")
    public ResponseEntity<Map<String, String>> updateEmail(
            @PathVariable("clientId") Long clientId,
            @RequestBody UpdateEmailRequestDto request
    ) {
        clientService.updateEmail(clientId, request.getEmail());
        return ResponseEntity.ok(Map.of("message", "이메일 변경 성공!"));
    }

    @PatchMapping("/introduction/{clientId}")
    @Operation(summary = "소개글 수정 API", description = "사용자의 소개글을 수정합니다.")
    public ResponseEntity<Map<String, String>> updateIntroduction(
            @PathVariable("clientId") Long clientId,
            @RequestBody UpdateIntroductionRequestDto request
    ) {
        clientService.updateIntroduction(clientId, request.getIntroduction());
        return ResponseEntity.ok(Map.of("message", "소개글 변경 성공!"));
    }

    @PatchMapping("/interest/{clientId}")
    @Operation(summary = "관심사 수정 API", description = "사용자의 관심사(topic1~3)를 수정합니다.")
    public ResponseEntity<Map<String, String>> updateInterest(
            @PathVariable("clientId") Long clientId,
            @RequestBody UpdateInterestRequestDto request
    ) {
        clientService.updateInterest(clientId, request.getTopic1(), request.getTopic2(), request.getTopic3());
        return ResponseEntity.ok(Map.of("message", "관심사 변경 성공!"));
    }

    @PatchMapping("/career/{clientId}")
    @Operation(summary = "경력 수정 API", description = "사용자의 경력을 수정합니다.")
    public ResponseEntity<Map<String, String>> updateCareer(
            @PathVariable("clientId") Long clientId,
            @RequestBody UpdateCareerRequestDto request
    ) {
        clientService.updateCareer(clientId, request.getCareer());
        return ResponseEntity.ok(Map.of("message", "경력 변경 성공!"));
    }

    @PatchMapping("/role/{clientId}")
    @Operation(summary = "직무 수정 API", description = "사용자의 직무를 수정합니다.")
    public ResponseEntity<Map<String, String>> updateRole(
            @PathVariable("clientId") Long clientId,
            @RequestBody UpdateRoleRequestDto request
    ) {
        clientService.updateRole(clientId, request.getRole());
        return ResponseEntity.ok(Map.of("message", "직무 변경 성공!"));
    }

    @PatchMapping("/links/{clientId}")
    @Operation(summary = "SNS링크 수정 API", description = "사용자의 SNS링크 리스트를 수정합니다.")
    public ResponseEntity<Map<String, String>> updateLinks(
            @PathVariable("clientId") Long clientId,
            @RequestBody UpdateLinksRequestDto request
    ) {
        clientService.updateLinks(clientId, request.getLinks());
        return ResponseEntity.ok(Map.of("message", "링크 변경 성공!"));
    }

    @PatchMapping(value = "/image/{clientId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 수정 API", description = "프로필 이미지를 수정합니다.")
    public ResponseEntity<Map<String, String>> updateProfileImage(
            @PathVariable("clientId") Long clientId,
            @RequestPart MultipartFile profileImage
    ) {
        clientService.updateProfileImage(clientId, profileImage);
        return ResponseEntity.ok(Map.of("message", "프로필 이미지 변경 성공!"));
    }

}
