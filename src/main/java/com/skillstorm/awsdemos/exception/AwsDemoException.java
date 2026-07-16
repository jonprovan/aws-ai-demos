package com.skillstorm.awsdemos.exception;

public class AwsDemoException extends RuntimeException {

    public AwsDemoException(String message) {
        super(message);
    }

    public AwsDemoException(String message, Throwable cause) {
        super(message, cause);
    }
}
