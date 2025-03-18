package com.goorm.team9.icontact.domain.lecture.controller;

import com.goorm.team9.icontact.domain.lecture.dto.request.LectureRequestDTO;
import com.goorm.team9.icontact.domain.lecture.dto.response.LectureResponseDTO;
import com.goorm.team9.icontact.domain.lecture.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
@Tag(name = "Lecture API", description = "강의 등록 및 조회 API")
public class LectureController {

    private final LectureService lectureService;

    @PostMapping
    @Operation(summary = "강의 등록 API", description = "컨퍼런스에 강의를 등록합니다.")
    public ResponseEntity<LectureResponseDTO> createLecture(
            @RequestBody LectureRequestDTO request
    ) {
        return ResponseEntity.ok(lectureService.createLecture(
                request.getTitle(), request.getLecturer(),
                request.getOpenTime(), request.getCloseTime(),
                request.getConferenceId()
        ));
    }

    @GetMapping("/conference")
    @Operation(summary = "컨퍼런스 강의 목록 조회 API", description = "특정 컨퍼런스의 모든 강의 목록을 조회합니다.")
    public ResponseEntity<List<LectureResponseDTO>> getLecturesByConference(
            @RequestParam Long conferenceId
    ) {
        return ResponseEntity.ok(lectureService.getLecturesByConference(conferenceId));
    }
}
