package com.goorm.team9.icontact.common.error;

public interface ErrorCodeInterface {
    Integer getHttpStatusCode();
    Integer getErrorCode();
    String getDescription();
}
