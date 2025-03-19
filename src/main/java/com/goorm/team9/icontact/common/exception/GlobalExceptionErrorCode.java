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
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "반경 내 일치하는 참가자가 없습니다.", 40403),
    DUPLICATE_CLIENT(HttpStatus.CONFLICT, "중복된 참가자의 정보가 존재합니다.", 40901),
    INVALID_LOCATION_DATA(HttpStatus.BAD_REQUEST, "유효하지 않은 위도/경도 값입니다. 다시 확인해 주세요.", 40002),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "가입된 참가자가 아닙니다. 다시 확인해 주세요.", 40004),
    INVALID_INTEREST(HttpStatus.BAD_REQUEST, "지정한 관심분야와 참가자가 입력한 관심분야가 일치하지 않습니다. 다시 확인해 주세요.", 40006),
    UNKNOWN_INTEREST(HttpStatus.NOT_FOUND, "해당 관심분야 찾을 수 없습니다: %s", 40404),
    MISSING_INTEREST(HttpStatus.BAD_REQUEST, "해당 참가자는 선택한 관심분야가 없으므로 더 이상 진행이 어렵습니다. 관리자에게 문의하세요.", 40007),
    ALREADY_SAVED_LOCATION(HttpStatus.BAD_REQUEST, "최초 위치데이터를 저장합니다. /refresh를 사용하세요.", 40008),
    NO_SAVED_LOCATION(HttpStatus.BAD_REQUEST, "최초 위치 데이터가 존재하지 않습니다. 먼저 /save를 통해 위치데이터를 저장하세요.", 40009);


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

    public String getFormattedMessage(Object... args) {
        return String.format(message, args);
    }
}
