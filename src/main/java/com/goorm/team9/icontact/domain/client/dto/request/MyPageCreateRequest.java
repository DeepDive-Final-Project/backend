package com.goorm.team9.icontact.domain.client.dto.request;

import com.goorm.team9.icontact.domain.client.enums.Career;
import com.goorm.team9.icontact.domain.client.enums.Framework;
import com.goorm.team9.icontact.domain.client.enums.Interest;
import com.goorm.team9.icontact.domain.client.enums.Language;
import com.goorm.team9.icontact.domain.client.enums.Status;
import com.goorm.team9.icontact.domain.client.enums.Role;
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
@Schema(description = "마이페이지 생성 요청 DTO", example = "{\n" +
        "  \"nickName\": \"Noah\",\n" +
        "  \"email\": \"noah@gmail.com\",\n" +
        "  \"role\": \"DEV\",\n" +
        "  \"career\": \"JUNIOR\",\n" +
        "  \"status\": \"PUBLIC\",\n" +
        "  \"introduction\": \"안녕하세요!\",\n" +
        "  \"link\": \"https://www.test.com\",\n" +
        "  \"topic1\": \"AI_Machine_Learning\",\n" +
        "  \"topic2\": \"Server_Development\",\n" +
        "  \"topic3\": \"API_Development\",\n" +
        "  \"language\": \"JAVA\",\n" +
        "  \"framework\": \"SPRINGBOOT\"\n" +
        "  \"provider\": \"kakao\"\n" +
        "}")
public class MyPageCreateRequest {
    @Schema(example = "Noah")
    private String nickName;

    @Schema(example = "noah@gmail.com")
    private String email;

    @Schema(example = "DEV")
    private Role role;

    @Schema(example = "JUNIOR")
    private Career career;

    @Schema(example = "PUBLIC")
    private Status status;

    @Schema(example = "안녕하세요!")
    private String introduction;

    @Schema(example = "[\"https://testlink1.com\", \"https://testlink2.com\"]")
    private List<String> links;

    @Schema(example = "AI_Machine_Learning")
    private Interest topic1;

    @Schema(example = "Server_Development")
    private Interest topic2;

    @Schema(example = "API_Development")
    private Interest topic3;

    @Schema(example = "JAVA")
    private Language language;

    @Schema(example = "SPRINGBOOT")
    private Framework framework;

    @Schema(example = "kakao")
    private String provider;
}
