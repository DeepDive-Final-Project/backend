package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Language implements EnumWithDescription {
    JAVA("JAVA"),
    PYTHON("PYTHON"),
    C("C"),
    C_SHARP("C#"),
    C_PLUS_PLUS("C++"),
    GO("GO"),
    RUBY("루비"),
    Node_js("Node.js"),
    Kotlin("코틀린"),
    PHP("PHP"),
    Rust("러스트"),
    HTML("HTML"),
    CSS("CSS"),
    JavaScript("자바스크립트"),
    TypeScript("타입스크립트");

    private final String description;

    Language(String description) {
        this.description = description;
    }
}

