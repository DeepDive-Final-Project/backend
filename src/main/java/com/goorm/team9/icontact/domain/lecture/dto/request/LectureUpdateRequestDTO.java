package com.goorm.team9.icontact.domain.lecture.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "강의 수정 요청 DTO")
public class LectureUpdateRequestDTO {

    @Schema(description = "수정할 강의 제목", example = "Advanced AI Techniques")
    private String title;

    @Schema(description = "수정할 강사 이름", example = "Dr. Smith")
    private String lecturer;

    @Schema(description = "수정할 시작 시간", example = "10:00")
    private String openTime;

    @Schema(description = "수정할 종료 시간", example = "11:00")
    private String closeTime;
}
