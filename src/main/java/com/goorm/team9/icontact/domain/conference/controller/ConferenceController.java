package com.goorm.team9.icontact.domain.conference.controller;

import com.goorm.team9.icontact.domain.conference.dto.request.ConferenceRequestDto;
import com.goorm.team9.icontact.domain.conference.dto.request.ConferenceUpdateRequestDto;
import com.goorm.team9.icontact.domain.conference.dto.response.ConferenceResponseDto;
import com.goorm.team9.icontact.domain.conference.enums.Day;
import com.goorm.team9.icontact.domain.conference.service.ConferenceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conferences")
@RequiredArgsConstructor
@Tag(name = "Conference API", description = "컨퍼런스 관련 API")
public class ConferenceController {

    private final ConferenceService conferenceService;

    @PostMapping
    @Operation(summary = "컨퍼런스 등록 API", description = "새로운 컨퍼런스를 등록합니다.")
    public ResponseEntity<ConferenceResponseDto> createConference(
            @RequestBody ConferenceRequestDto request
    ) {
        return ResponseEntity.ok(conferenceService.createConference(request.getName(), request.getDay()));
    }


    @GetMapping("/day")
    @Operation(summary = "특정 일자의 컨퍼런스 조회 API", description = "DAY_1 ~ DAY_5 중 선택")
    public ResponseEntity<List<ConferenceResponseDto>> getConferencesByDay(
            @RequestParam Day day
    ) {
        return ResponseEntity.ok(conferenceService.getConferencesByDay(day));
    }

    @PutMapping("/{conferencesId}")
    @Operation(summary = "컨퍼런스 수정 API", description = "컨퍼런스 이름과 날짜를 수정합니다.")
    public ResponseEntity<ConferenceResponseDto> updateConference(
            @PathVariable Long conferencesId,
            @RequestBody ConferenceUpdateRequestDto request
    ) {
        return ResponseEntity.ok(conferenceService.updateConference(conferencesId, request.getName(), request.getDay()));

    }

    @DeleteMapping("/{conferencesId}")
    @Operation(summary = "컨퍼런스 삭제 API", description = "컨퍼런스를 삭제합니다.")
    public ResponseEntity<Void> deleteConference(@PathVariable Long conferencesId) {
        conferenceService.deleteConference(conferencesId);
        return ResponseEntity.noContent().build();

    }

}
