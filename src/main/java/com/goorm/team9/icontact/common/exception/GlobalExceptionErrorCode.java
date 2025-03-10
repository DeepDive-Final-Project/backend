package com.goorm.team9.icontact.common.exception;

import com.goorm.team9.icontact.common.error.ErrorCodeInterface;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalExceptionErrorCode implements ErrorCodeInterface {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", 50001),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 형식의 요청입니다.", 40001),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", 40101),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근이 거부되었습니다.", 40301),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", 40401),
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 참가자를 찾을 수 없습니다.", 40402),
    DUPLICATE_CLIENT(HttpStatus.CONFLICT, "중복된 참가자의 정보가 존재합니다.", 40901),
    INVALID_LOCATION_DATA(HttpStatus.BAD_REQUEST, "유효하지 않은 위도/경도 값입니다.", 40002),
    GPS_ERROR(HttpStatus.BAD_REQUEST, "GPS 데이터가 올바르지 않습니다.", 40003),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 ID입니다.", 40004);

    private final HttpStatus httpStatus;
    private final String message;
    private final int errorCode;

    GlobalExceptionErrorCode(HttpStatus httpStatus, String message, int errorCode) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public Integer getHttpStatusCode() {
        return httpStatus.value();
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getDescription() {
        return message;
    }
}
