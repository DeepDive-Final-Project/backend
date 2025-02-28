package com.goorm.team9.icontact.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final GlobalExceptionErrorCode errorCode;

    public CustomException(GlobalExceptionErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
