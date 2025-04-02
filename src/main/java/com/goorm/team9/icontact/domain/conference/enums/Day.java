package com.goorm.team9.icontact.domain.conference.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Day implements EnumWithDescription {

    DAY_1("컨퍼런스 1일차"),
    DAY_2("컨퍼런스 2일차"),
    DAY_3("컨퍼런스 3일차"),
    DAY_4("컨퍼런스 4일차"),
    DAY_5("컨퍼런스 5일차");

    private final String description;

    Day (String description) {
        this.description = description;
    }

}
