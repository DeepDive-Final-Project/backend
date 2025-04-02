package com.goorm.team9.icontact.domain.client.controller;

import com.goorm.team9.icontact.domain.client.enums.InterestCategory;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.service.ClientEnumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/enums")
@RequiredArgsConstructor
@Tag(name = "Enum List API", description = "마이페이지 작성에 사용될 드롭다운 항목들을 관리하는 API 입니다.")
public class EnumController {

    private final ClientEnumService clientEnumService;

    @GetMapping("/roles")
    @Operation(summary = "직업 형태 API", description = "직업의 종류를 출력하는 API입니다.")
    public ResponseEntity<List<Map<String, Object>>> getRoles() {
        return ResponseEntity.ok(clientEnumService.getEnumListWithApiCode(Role.values()));
    }

    @GetMapping("/careers")
    @Operation(summary = "경력 API", description = "선택한 직업에 따라 필터링된 경력 목록을 반환하는 API입니다.")
    public ResponseEntity<List<Map<String, String>>> getCareers(
            @RequestParam Role role
    ) {
        return ResponseEntity.ok(clientEnumService.getFilteredCareers(role));
    }

    @GetMapping("/interests")
    @Operation(summary = "관심 주제 API", description = "선택한 분야(DEV, PD, DS)에 따라 관심 주제 목록을 반환합니다.")
    public ResponseEntity<List<Map<String, String>>> getInterests(
            @RequestParam InterestCategory category
    ) {
        return ResponseEntity.ok(clientEnumService.getFilteredInterestsByApiCode(category.name()));
    }

}