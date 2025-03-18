package com.goorm.team9.icontact.domain.conference.dto.request;

import com.goorm.team9.icontact.domain.conference.enums.Day;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "컨퍼런스 등록 요청 DTO", example = "{\n" +
        "  \"name\": \"AI Future Summit\",\n" +
        "  \"day\": \"DAY_1\"\n" +
        "}")
public class ConferenceRequestDTO {

    @Schema(description = "컨퍼런스 이름", example = "AI Future Summit")
    private String name;

    @Schema(description = "컨퍼런스 일자 (DAY_1 ~ DAY_5)", example = "DAY_1")
    private Day day;
}
