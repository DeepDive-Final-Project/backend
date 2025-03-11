package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Career implements EnumWithDescription {
    JUNIOR("주니어 개발자", "DEV"),
    SENIOR("시니어 개발자", "DEV"),
    MIDDLE("미들 개발자", "DEV"),
    HIGH_SCHOOL_STUDENT("고등학생", "STUDENT"),
    UNIVERSITY_STUDENT("대학생", "STUDENT"),
    ECT("기타", "ECT" );

    private final String description;
    private final String apiCode;

    Career(String description, String apiCode) {
        this.description = description;
        this.apiCode = apiCode;
    }
}
