package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Status implements EnumWithDescription {

    PUBLIC("공개"),
    PRIVATE("비공개");

    private final String description;

    Status(String description) {
        this.description = description;
    }
}
