package com.goorm.team9.icontact.domain.client.controller;

import com.goorm.team9.icontact.domain.client.dto.request.MemoRequestDTO;
import com.goorm.team9.icontact.domain.client.dto.response.MemoResponseDTO;
import com.goorm.team9.icontact.domain.client.service.MemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/memo")
@RequiredArgsConstructor
@Tag(name = "Memo API", description = "메모 작성 및 조회, 삭제 API")
public class MemoController {

    private final MemoService memoService;

    @PostMapping("/{writerId}")
    @Operation(summary = "메모 작성 API", description = "특정 대상에게 메모를 작성합니다.")
    public ResponseEntity<MemoResponseDTO> createMemo(
            @PathVariable Long writerId,
            @RequestBody MemoRequestDTO request
    ) {
        return ResponseEntity.ok(memoService.createMemo(writerId, request.getTargetId(), request.getContent()));
    }


    @GetMapping("/{writerId}")
    @Operation(summary = "작성 메모 목록 조회 API", description = "작성자가 작성한 메모 목록을 확인합니다.")
    public ResponseEntity<List<MemoResponseDTO>> getMemosByWriter(
            @PathVariable Long writerId
    ) {
        return ResponseEntity.ok(memoService.getMemosByWriter(writerId));
    }

    @DeleteMapping("/{memoId}")
    @Operation(summary = "메모 삭제 API", description = "메모를 삭제합니다.")
    public ResponseEntity<Void> deleteMemo(
            @PathVariable Long memoId
    ) {
        memoService.deleteMemo(memoId);
        return ResponseEntity.noContent().build();
    }
}
