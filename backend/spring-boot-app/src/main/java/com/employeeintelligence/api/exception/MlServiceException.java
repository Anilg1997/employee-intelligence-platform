package com.employeeintelligence.api.exception;

public class MlServiceException extends RuntimeException {

    public MlServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}