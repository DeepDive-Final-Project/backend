package com.goorm.team9.icontact.domain.client.controller;

import com.goorm.team9.icontact.domain.client.enums.Career;
import com.goorm.team9.icontact.domain.client.enums.Interest;
import com.goorm.team9.icontact.domain.client.enums.Role;
import com.goorm.team9.icontact.domain.client.enums.Status;
import com.goorm.team9.icontact.domain.client.enums.Language;
import com.goorm.team9.icontact.domain.client.enums.Framework;
import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client/enums")
@RequiredArgsConstructor
@Tag(name = "Enum List API", description = "마이페이지 작성에 사용될 드롭다운 항목들을 관리하는 API 입니다.")
public class EnumController {

    @GetMapping("/roles")
    @Operation(summary = "직업 형태 API", description = "직업의 종류를 출력하는 API입니다.")
    public ResponseEntity<List<Map<String, Object>>> getRoles() {
        return ResponseEntity.ok(getEnumListWithApiCode(Role.values()));
    }

    @GetMapping("/statuses")
    @Operation(summary = "공개여부 API", description = "공개 여부의 종류를 출력하는 API입니다.")
    public ResponseEntity<List<Map<String, String>>> getStatuses() {
        return ResponseEntity.ok(getEnumList(Status.values()));
    }

    @GetMapping("/careers")
    @Operation(summary = "경력 API", description = "선택한 직업에 따라 필터링된 경력 목록을 반환하는 API입니다.")
    public ResponseEntity<List<Map<String, String>>> getCareers(@RequestParam Role role) {
        return ResponseEntity.ok(getFilteredCareers(role));
    }


    @GetMapping("/frameworks")
    @Operation(summary = "프레임워크 API", description = "프레임워크의 종류를 출력하는 API입니다.")
    public ResponseEntity<List<Map<String, String>>> getFrameworks() {
        return ResponseEntity.ok(getEnumList(Framework.values()));
    }

    @GetMapping("/languages")
    @Operation(summary = "언어 API", description = "언어의 종류를 출력하는 API입니다.")
    public ResponseEntity<List<Map<String, String>>> getLanguage() {
        return ResponseEntity.ok(getEnumList(Language.values()));
    }

    @GetMapping("/interests")
    @Operation(summary = "관심 주제 API", description = "관심 주제의 종류를 출력하는 API입니다.")
    public ResponseEntity<List<Map<String, String>>> getInterest() {
        return ResponseEntity.ok(getEnumList(Interest.values()));
    }

    private <E extends Enum<E> & EnumWithDescription> List<Map<String, String>> getEnumList(E[] values) {
        return Arrays.stream(values)
                .map(e -> Map.of("key", e.name(), "description", e.getDescription()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getEnumListWithApiCode(Role[] values) {
        return Arrays.stream(values)
                .map(e -> Map.<String, Object>of(
                        "key", e.name(),
                        "description", e.getDescription(),
                        "apiCode", Integer.valueOf(e.getApiCode())
                ))
                .collect(Collectors.toList());
    }

    private List<Map<String, String>> getFilteredCareers(Role role) {
        int apiCode = role.getApiCode();

        return Arrays.stream(Career.values())
                .filter(c -> c.getApiCode() == apiCode)
                .map(c -> Map.of("key", c.name(), "description", c.getDescription()))
                .collect(Collectors.toList());
    }
}

