package com.goorm.team9.icontact.domain.client.controller;

import com.goorm.team9.icontact.domain.client.dto.request.MyPageCreateRequest;
import com.goorm.team9.icontact.domain.client.dto.request.MyPageUpdateRequest;
import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDTO;
import com.goorm.team9.icontact.domain.client.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/client/profile")
@RequiredArgsConstructor
@Tag(name = "MyPage API", description = "마이페이지 생성과 관련된 API 입니다.")
public class ClientController {

    private final ClientService clientService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "마이페이지 생성 API", description = "사용자 정보를 입력하여 마이페이지를 생성합니다.")
    public ResponseEntity<ClientResponseDTO> createMyPage(
            @RequestPart("clientInfo") MyPageCreateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return ResponseEntity.ok(clientService.createMyPage(
                request.getNickName(), request.getEmail(), request.getRole(),
                request.getCareer(), request.getStatus(), request.getIntroduction(),
                request.getLink(), profileImage,
                request.getTopic1(), request.getTopic2(), request.getTopic3(),
                request.getLanguage(), request.getFramework()
        ));
    }

    @GetMapping("/{client_Id}")
    @Operation(summary = "사용자 정보 출력 API", description = "다른 사용자의 정보를 확인합니다.")
    public ResponseEntity<ClientResponseDTO> getUserById(
            @PathVariable Long client_Id
    ) {
        return ResponseEntity.ok(clientService.getUserById(client_Id));
    }

    @PatchMapping(value = "/update/{client_Id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "마이페이지 수정 API", description = "본인의 정보를 수정합니다.")
    public ResponseEntity<ClientResponseDTO> updateUser(
            @PathVariable Long client_Id,
            @RequestPart("clientInfo") MyPageUpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return ResponseEntity.ok(clientService.updateUser(
                client_Id, request.getNickName(), request.getRole(), request.getCareer(),
                request.getStatus(), request.getIntroduction(), request.getLink(), profileImage,
                request.getTopic1(), request.getTopic2(), request.getTopic3(),
                request.getLanguage(), request.getFramework()
        ));
    }

}
