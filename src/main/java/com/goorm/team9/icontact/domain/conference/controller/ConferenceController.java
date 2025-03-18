package com.goorm.team9.icontact.domain.conference.controller;

import com.goorm.team9.icontact.domain.conference.dto.response.ConferenceResponseDTO;
import com.goorm.team9.icontact.domain.conference.enums.Day;
import com.goorm.team9.icontact.domain.conference.service.ConferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conferences")
@RequiredArgsConstructor
@Tag(name = "Conference API", description = "컨퍼런스 관련 API")
public class ConferenceController {

    private final ConferenceService conferenceService;

    @PostMapping
    @Operation(summary = "컨퍼런스 등록", description = "새로운 컨퍼런스를 등록합니다.")
    public ResponseEntity<ConferenceResponseDTO> createConference(
            @RequestParam String name,
            @RequestParam Day day
    ) {
        return ResponseEntity.ok(conferenceService.createConference(name, day));
    }

    @GetMapping("/day")
    @Operation(summary = "특정 일자의 컨퍼런스 조회", description = "DAY_1 ~ DAY_5 중 선택")
    public ResponseEntity<List<ConferenceResponseDTO>> getConferencesByDay(
            @RequestParam Day day
    ) {
        return ResponseEntity.ok(conferenceService.getConferencesByDay(day));
    }
}


