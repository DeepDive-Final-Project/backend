package com.goorm.team9.icontact.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LectureErrorCode implements ErrorCodeInterface {

    LECTURE_NOT_FOUND(404, 4001, "해당 컨퍼런스를 찾을 수 없습니다."),
    CONFERENCE_NOT_FOUND(404, 4002, "해당 컨퍼런스를 찾을 수 없습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String description;

}

