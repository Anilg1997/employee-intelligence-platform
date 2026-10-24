package com.employeeintelligence.api.exception;

public class InvalidPredictionInputException extends RuntimeException {

    public InvalidPredictionInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
