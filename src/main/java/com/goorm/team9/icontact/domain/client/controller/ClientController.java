package com.goorm.team9.icontact.domain.client.controller;

import com.goorm.team9.icontact.domain.client.dto.response.ClientResponseDTO;
import com.goorm.team9.icontact.domain.client.enums.*;
import com.goorm.team9.icontact.domain.client.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "마이페이지 생성 API", description = "사용자 정보를 입력하여 마이페이지 생성합니다.")
    public ResponseEntity<ClientResponseDTO> createMyPage(
            @Parameter(description = "닉네임", example = "Noah")
            @RequestParam String nickName,
            @Parameter(description = "이메일", example = "noah@gmail.com")
            @RequestParam String email,
            @Parameter(description = "직무", example = "DEV")
            @RequestParam Role role,
            @Parameter(description = "경력", example = "JUNIOR")
            @RequestParam Career career,
            @Parameter(description = "공개 여부", example = "PUBLIC")
            @RequestParam Status status,
            @Parameter(description = "소개글", example = "안녕하세요! 개발자입니다!")
            @RequestParam(required = false) String introduction,
            @Parameter(description = "링크", example = "https://www.test.com")
            @RequestParam(required = false) String link,
            @Parameter(description = "관심 주제 1", example = "AI_Machine_Learning")
            @RequestParam Interest topic1,
            @Parameter(description = "관심 주제 2", example = "Server_Development")
            @RequestParam Interest topic2,
            @Parameter(description = "관심 주제 3", example = "API_Development")
            @RequestParam Interest topic3,
            @Parameter(description = "주력 언어", example = "JAVA")
            @RequestParam Language language,
            @Parameter(description = "프레임워크", example = "SPRINGBOOT")
            @RequestParam Framework framework,
            @Parameter(description = "프로필 이미지 파일", required = false)
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage

    ) {
        return ResponseEntity.ok(clientService.createMyPage(
                nickName, email, role, career, status, introduction, link, profileImage,
                topic1, topic2, topic3, language, framework
        ));
    }


    @GetMapping("/{clientId}")
    @Operation(summary = "사용자 정보 출력 API", description = "다른 사용자의 정보를 확인합니다.")
    public ResponseEntity<ClientResponseDTO> getUserById(
            @PathVariable Long clientId
    ) {
        return ResponseEntity.ok(clientService.getUserById(clientId));
    }

    @PatchMapping(value = "/update/{clientId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "마이페이지 수정 API", description = "RequestParam으로 사용자 정보를 입력하고 이미지를 첨부하여 마이페이지 수정")
    public ResponseEntity<ClientResponseDTO> updateUser(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "닉네임", example = "UpdatedNoah")
            @RequestParam(required = false) String nickName,
            @Parameter(description = "직무", example = "DEV")
            @RequestParam(required = false) Role role,
            @Parameter(description = "경력", example = "JUNIOR")
            @RequestParam(required = false) Career career,
            @Parameter(description = "공개 여부", example = "PUBLIC")
            @RequestParam(required = false) Status status,
            @Parameter(description = "소개글", example = "안녕하세요! 수정된 개발자입니다!")
            @RequestParam(required = false) String introduction,
            @Parameter(description = "링크", example = "https://updated-link.com")
            @RequestParam(required = false) String link,
            @Parameter(description = "관심 주제 1", example = "AI_Machine_Learning")
            @RequestParam(required = false) Interest topic1,
            @Parameter(description = "관심 주제 2", example = "Server_Development")
            @RequestParam(required = false) Interest topic2,
            @Parameter(description = "관심 주제 3", example = "API_Development")
            @RequestParam(required = false) Interest topic3,
            @Parameter(description = "주력 언어", example = "JAVA")
            @RequestParam(required = false) Language language,
            @Parameter(description = "프레임워크", example = "SPRINGBOOT")
            @RequestParam(required = false) Framework framework,
            @Parameter(description = "프로필 이미지 파일", required = false)
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return ResponseEntity.ok(clientService.updateUser(
                clientId, nickName, role, career, status, introduction, link, profileImage,
                topic1, topic2, topic3, language, framework
        ));
    }


}
