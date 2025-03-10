package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Industry implements EnumWithDescription {

    PD("프로덕트 디자이너"),
    FE("프론트엔드"),
    BE("백엔드"),
    PM("프로덕트 매니지먼트"),
    ETC("기타");

    private final String description;

    Industry(String description) {
        this.description = description;
    }
}
