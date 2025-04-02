package com.goorm.team9.icontact.common.exception;

import com.goorm.team9.icontact.common.error.ErrorCodeInterface;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCodeInterface errorCode;

    public CustomException(ErrorCodeInterface errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

}
