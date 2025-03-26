package com.goorm.team9.icontact.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ClientErrorCode implements ErrorCodeInterface {

    // Client 관련 커스텀 에러는 10XX 의 형식을 사용
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 1001, "해당 프로필을 찾을 수 없습니다."),
    STATUS_IS_PRIVATE(HttpStatus.FORBIDDEN.value(), 1002, "해당 프로필은 비공개 상태입니다."),
    EXISTED_EMAIL(HttpStatus.CONFLICT.value(), 1003, "이미 존재하는 이메일입니다."),
    NO_CHAT_OPPORTUNITY(HttpStatus.BAD_REQUEST.value(), 1004, "더 이상 채팅방을 만들 수 없습니다."),
    CHAT_OPPORTUNITY_FULL(HttpStatus.BAD_REQUEST.value(), 1005, "채팅 기회가 이미 최대치입니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String description;

    public String getMessage() {
        return description;
    }
}
