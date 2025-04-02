package com.goorm.team9.icontact.domain.lecture.controller;

import com.goorm.team9.icontact.domain.lecture.dto.request.LectureRequestDto;
import com.goorm.team9.icontact.domain.lecture.dto.request.LectureUpdateRequestDto;
import com.goorm.team9.icontact.domain.lecture.dto.response.LectureResponseDto;
import com.goorm.team9.icontact.domain.lecture.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
@Tag(name = "Lecture API", description = "강의 등록 및 조회 API")
public class LectureController {

    private final LectureService lectureService;

    @PostMapping
    @Operation(summary = "강의 등록 API", description = "컨퍼런스에 강의를 등록합니다.")
    public ResponseEntity<LectureResponseDto> createLecture(
            @RequestBody LectureRequestDto request
    ) {
        return ResponseEntity.ok(lectureService.createLecture(
                request.getTitle(), request.getLecturer(),
                request.getOpenTime(), request.getCloseTime(),
                request.getConferenceId()
        ));
    }

    @GetMapping("/{conferenceId}")
    @Operation(summary = "컨퍼런스 강의 목록 조회 API", description = "특정 컨퍼런스의 모든 강의 목록을 조회합니다.")
    public ResponseEntity<List<LectureResponseDto>> getLecturesByConference(
            @RequestParam Long conferenceId
    ) {
        return ResponseEntity.ok(lectureService.getLecturesByConference(conferenceId));
    }

    @PutMapping("/{lectureId}")
    @Operation(summary = "강의 수정 API", description = "강의 제목, 강사, 시간을 수정합니다.")
    public ResponseEntity<LectureResponseDto> updateLecture(
            @PathVariable Long lectureId,
            @RequestBody LectureUpdateRequestDto request
    ) {
        return ResponseEntity.ok(lectureService.updateLecture(
                lectureId, request.getTitle(), request.getLecturer(), request.getOpenTime(), request.getCloseTime()
        ));
    }

    @DeleteMapping("/{lectureId}")
    @Operation(summary = "강의 삭제 API", description = "특정 강의를 삭제합니다.")
    public ResponseEntity<Void> deleteLecture(
            @PathVariable Long lectureId
    ) {
        lectureService.deleteLecture(lectureId);
        return ResponseEntity.noContent().build();
    }

}
