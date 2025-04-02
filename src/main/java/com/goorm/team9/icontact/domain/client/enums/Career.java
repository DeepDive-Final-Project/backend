package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Career implements EnumWithDescription {

    JUNIOR("주니어", 1),
    MIDDLE("미들", 1),
    SENIOR("시니어", 1),
    JOB_SEEKER("취업준비생", 1),
    HIGH_SCHOOL_STUDENT("고등학생", 2),
    UNIVERSITY_STUDENT("대학생", 2),
    GRADUATE_STUDENT("대학원생", 2),
    ECT("기타", 3);

    private final String description;
    private final int apiCode;

    Career(String description, int apiCode) {
        this.description = description;
        this.apiCode = apiCode;
    }

    public static Career fromDescription(String description) {
        return Arrays.stream(values())
                .filter(c -> c.description.equals(description))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Career description: " + description));
    }

}
