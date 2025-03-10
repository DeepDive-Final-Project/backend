package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Career implements EnumWithDescription {
    JUNIOR("주니어 개발자"),
    SENIOR("시니어 개발자"),
    MIDDLE("미들 개발자"),
    HIGH_SCHOOL_STUDENT("고등학생"),
    UNIVERSITY_STUDENT("대학생"),
    ECT("기타");

    private final String description;

    Career(String description) {
        this.description = description;
    }
}
