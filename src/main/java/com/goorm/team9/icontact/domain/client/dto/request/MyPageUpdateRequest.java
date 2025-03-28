package com.goorm.team9.icontact.domain.client.dto.request;

import com.goorm.team9.icontact.domain.client.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Schema(description = "마이페이지 수정 요청 DTO", example = "{\n" +
        "  \"nickName\": \"NoahUpdated\",\n" +
        "  \"role\": \"DEV\",\n" +
        "  \"career\": \"SENIOR\",\n" +
        "  \"status\": \"PUBLIC\",\n" +
        "  \"introduction\": \"업데이트된 소개입니다!\",\n" +
        "  \"link\": \"https://updated.com\",\n" +
        "  \"topic1\": \"Cloud_Computing\",\n" +
        "  \"topic2\": \"Data_Science\",\n" +
        "  \"topic3\": \"DevOps\",\n" +
        "  \"language\": \"PYTHON\",\n" +
        "  \"framework\": \"DJANGO\"\n" +
        "}")
public class MyPageUpdateRequest {
    @Schema(example = "NoahUpdated")
    private String nickName;

    @Schema(example = "DEV")
    private Role role;

    @Schema(example = "SENIOR")
    private Career career;

    @Schema(example = "PUBLIC")
    private Status status;

    @Schema(example = "업데이트된 소개입니다!")
    private String introduction;

    @Schema(example = "[\"https://updatedlink1.com\", \"https://updatedlink2.com\"]")
    private List<String> links;


    @Schema(example = "Cloud_Computing")
    private Interest topic1;

    @Schema(example = "Data_Science")
    private Interest topic2;

    @Schema(example = "DevOps")
    private Interest topic3;

    @Schema(example = "PYTHON")
    private Language language;

    @Schema(example = "DJANGO")
    private Framework framework;
}

