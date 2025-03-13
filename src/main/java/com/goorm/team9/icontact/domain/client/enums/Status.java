package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Status implements EnumWithDescription {

    PUBLIC("공개"),
    PRIVATE("비공개");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public static Status fromDescription(String description) {
        return Arrays.stream(values())
                .filter(c -> c.description.equals(description))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Role description: " + description));
    }
}