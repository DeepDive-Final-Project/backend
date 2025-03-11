package com.goorm.team9.icontact.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.goorm.team9.icontact.common.error.ErrorCodeInterface;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final Object data;
    private final String message;

    public static ErrorResponse of(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .data(null)
                .message(message)
                .build();
    }

    public static ErrorResponse of(ErrorCodeInterface errorCode) {
        return ErrorResponse.builder()
                .data(null)
                .message(errorCode.getDescription())
                .build();
    }
}
