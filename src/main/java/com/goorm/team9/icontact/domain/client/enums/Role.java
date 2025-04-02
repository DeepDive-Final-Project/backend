package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Role implements EnumWithDescription {

    PD("디자이너", 1),
    PM("프로덕트 매니지먼트", 1),
    DEV("개발자", 1),
    STUDENT("학생", 2),
    ETC("기타", 3);

    private final String description;
    private final int apiCode;

    Role(String description, int apiCode) {
        this.description = description;
        this.apiCode = apiCode;
    }

    public static Role fromDescription(String description) {
        return Arrays.stream(values())
                .filter(c -> c.description.equals(description))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Role description: " + description));
    }

}
