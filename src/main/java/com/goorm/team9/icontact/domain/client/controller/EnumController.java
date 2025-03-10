package com.goorm.team9.icontact.domain.client.controller;

import com.goorm.team9.icontact.domain.client.enums.Career;
import com.goorm.team9.icontact.domain.client.enums.Interest;
import com.goorm.team9.icontact.domain.client.enums.Industry;
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
    public ResponseEntity<List<Map<String, String>>> getRoles() {
        return ResponseEntity.ok(getEnumList(Role.values()));
    }

    @GetMapping("/statuses")
    @Operation(summary = "공개여부 API", description = "공개 여부의 종류를 출력하는 API입니다.")
    public ResponseEntity<List<Map<String, String>>> getStatuses() {
        return ResponseEntity.ok(getEnumList(Status.values()));
    }

    @GetMapping("/industries")
    @Operation(summary = "분야 API", description = "직업 분야의 종류를 출력하는 API입니다.")
    public ResponseEntity<List<Map<String, String>>> getIndustries() {
        return ResponseEntity.ok(getEnumList(Industry.values()));
    }

    @GetMapping("/careers")
    @Operation(summary = "경력 API", description = "경력의 종류를 출력하는 API입니다.")
    public ResponseEntity<List<Map<String, String>>> getCareers() {
        return ResponseEntity.ok(getEnumList(Career.values()));
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
                .map(e -> Map.of("key", e.name(), "description", e.getDescription()))  // 🔥 한글 설명 적용!
                .collect(Collectors.toList());
    }

}

