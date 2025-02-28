package com.goorm.team9.icontact.common.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;

    public static ErrorResponse of(GlobalExceptionErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .message(errorCode.getMessage())
                .build();
    }

    public static ErrorResponse of(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
    }
}
