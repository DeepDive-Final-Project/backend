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

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "마이페이지 생성 API", description = "사용자 정보를 개별 입력하여 마이페이지를 생성합니다.")
    public ResponseEntity<ClientResponseDTO> createMyPage(
            @Parameter(description = "닉네임", example = "Noah")
            @RequestParam String nickName,
            @Parameter(description = "나이", example = "27")
            @RequestParam Long age,
            @Parameter(description = "이메일", example = "goorm@gmail.com")
            @RequestParam String email,
            @Parameter(description = "분야")
            @RequestParam Industry industry,
            @Parameter(description = "직업")
            @RequestParam Role role,
            @Parameter(description = "경력")
            @RequestParam Career career,
            @Parameter(description = "공개여부")
            @RequestParam Status status,
            @Parameter(description = "소개글", example = "안녕하세요. 개발자입니다!")
            @RequestParam(required = false) String introduction,
            @Parameter(description = "접근 링크", example = "https://www.test.com")
            @RequestParam(required = false) String link,
            @Parameter(description = "프로필 이미지")
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @Parameter(description = "관심 주제 1")
            @RequestParam Interest topic1,
            @Parameter(description = "관심 주제 2")
            @RequestParam Interest topic2,
            @Parameter(description = "관심 주제 3")
            @RequestParam Interest topic3,
            @Parameter(description = "주력 언어")
            @RequestParam Language language,
            @Parameter(description = "주력 프레임워크")
            @RequestParam Framework framework
    ) {
        return ResponseEntity.ok(clientService.createMyPage(
                nickName, age, email, industry, role, career, status, introduction, link, profileImage,
                topic1, topic2, topic3, language, framework
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
            @Parameter(description = "아이디 고유 번호", example = "1")
            @PathVariable Long client_Id,
            @Parameter(description = "닉네임", example = "Noah2222")
            @RequestParam(required = false) String nickName,
            @Parameter(description = "나이", example = "27")
            @RequestParam(required = false) Long age,
            @Parameter(description = "분야")
            @RequestParam(required = false) Industry industry,
            @Parameter(description = "직업")
            @RequestParam(required = false) Role role,
            @Parameter(description = "경력")
            @RequestParam(required = false) Career career,
            @Parameter(description = "공개 여부")
            @RequestParam(required = false) Status status,
            @Parameter(description = "소개글", example = "안녕하세요. 바뀐 개발자입니다.")
            @RequestParam(required = false) String introduction,
            @Parameter(description = "접근 링크", example = "https://www.test22.com")
            @RequestParam(required = false) String link,
            @Parameter(description = "프로필 이미지")
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @Parameter(description = "관심 주제 1")
            @RequestParam(required = false) Interest topic1,
            @Parameter(description = "관심 주제 2")
            @RequestParam(required = false) Interest topic2,
            @Parameter(description = "관심 주제 3")
            @RequestParam(required = false) Interest topic3,
            @Parameter(description = "주략 언어")
            @RequestParam(required = false) Language language,
            @Parameter(description = "주력 프레임워크")
            @RequestParam(required = false) Framework framework
    ) {
        return ResponseEntity.ok(clientService.updateUser(
                client_Id, nickName, age, industry, role, career, status, introduction, link, profileImage,
                topic1, topic2, topic3, language, framework
        ));
    }
}
