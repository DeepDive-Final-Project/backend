package com.goorm.team9.icontact.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemoErrorCode implements ErrorCodeInterface {

    MEMO_WRITER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 2001, "작성자 정보를 찾을 수 없습니다."),
    MEMO_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 2002, "대상 사용자 정보를 찾을 수 없습니다."),
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 2003, "해당 메모를 찾을 수 없습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String description;

}
