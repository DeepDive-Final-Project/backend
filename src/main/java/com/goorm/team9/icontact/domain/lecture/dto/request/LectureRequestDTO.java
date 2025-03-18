package com.goorm.team9.icontact.domain.lecture.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "강의 등록 요청 DTO")
public class LectureRequestDTO {

    @Schema(description = "강의 제목", example = "AI와 미래 기술")
    private String title;

    @Schema(description = "강사명", example = "김노아")
    private String lecturer;

    @Schema(description = "시작 시간 (HH:mm)", example = "10:00")
    private String openTime;

    @Schema(description = "종료 시간 (HH:mm)", example = "11:30")
    private String closeTime;

    @Schema(description = "컨퍼런스 ID", example = "1")
    private Long conferenceId;
}
