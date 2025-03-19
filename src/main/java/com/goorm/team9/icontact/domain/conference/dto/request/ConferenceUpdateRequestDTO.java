package com.goorm.team9.icontact.domain.conference.dto.request;

import com.goorm.team9.icontact.domain.conference.enums.Day;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "컨퍼런스 수정 요청 DTO")
public class ConferenceUpdateRequestDTO {

    @Schema(description = "변경할 컨퍼런스 이름", example = "Updated Conference Name")
    private String name;

    @Schema(description = "변경할 컨퍼런스 날짜", example = "DAY_3")
    private Day day;
}
