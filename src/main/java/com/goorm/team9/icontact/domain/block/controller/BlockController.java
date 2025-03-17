package com.goorm.team9.icontact.domain.block.controller;

import com.goorm.team9.icontact.domain.block.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Block API", description = "차단 및 차단 해제 API 입니다.")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @Operation(summary = "사용자 차단 API", description = "사용자를 차단합니다.")
    @PostMapping("/block")
    public ResponseEntity<String> blockUser(@RequestParam String blockerNickname, @RequestParam String blockedNickname) {
        blockService.blockUser(blockerNickname, blockedNickname);
        return ResponseEntity.ok("사용자가 차단되었습니다.");
    }

    @Operation(summary = "사용자 차단 해제 API", description = "사용자를 차단 해제합니다.")
    @PostMapping("/unblock")
    public ResponseEntity<String> unblockUser(@RequestParam String blockerNickname, @RequestParam String blockedNickname) {
        blockService.unblockUser(blockerNickname, blockedNickname);
        return ResponseEntity.ok("사용자 차단이 해제되었습니다.");
    }
}
