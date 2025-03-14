package com.goorm.team9.icontact.domain.client.controller;

import com.goorm.team9.icontact.domain.client.dto.response.MemoResponseDTO;
import com.goorm.team9.icontact.domain.client.service.MemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memo")
@RequiredArgsConstructor
@Tag(name = "Memo API", description = "사용자 메모 관련 API")
public class MemoController {

    private final MemoService memoService;

    @PostMapping("/create")
    @Operation(summary = "메모 작성", description = "특정 사용자에게 개인 메모를 남깁니다.")
    public ResponseEntity<MemoResponseDTO> createMemo(
            @Parameter(description = "작성자 ID") @RequestParam Long writerId,
            @Parameter(description = "대상 사용자 ID") @RequestParam Long targetId,
            @Parameter(description = "메모 내용") @RequestParam String content) {
        return ResponseEntity.ok(memoService.createMemo(writerId, targetId, content));
    }

    @GetMapping("/writer/{writerId}")
    @Operation(summary = "내가 남긴 메모 조회", description = "내가 남긴 모든 메모를 가져옵니다.")
    public ResponseEntity<List<MemoResponseDTO>> getMemosByWriter(@PathVariable Long writerId) {
        return ResponseEntity.ok(memoService.getMemosByWriter(writerId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "메모 삭제", description = "메모를 삭제합니다.")
    public ResponseEntity<Void> deleteMemo(@Parameter(description = "메모 ID") @RequestParam Long memoId) {
        memoService.deleteMemo(memoId);
        return ResponseEntity.noContent().build();
    }
}
