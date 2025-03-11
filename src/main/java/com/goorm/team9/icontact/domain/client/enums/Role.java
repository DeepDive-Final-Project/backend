package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Role implements EnumWithDescription {

    STUDENT("학생"),
    WORKER("직장인"),
    FREELANCER("프리랜서"),
    JOB_SEEKER("취업준비생"),
    ECT("기타");

    private final String description;

    Role(String description) {
        this.description = description;
    }
}
