package com.skillstorm.awsdemos.exception;

/** Wraps demo-specific failures (bad input, job timeout/failure) that aren't already an AWS SDK exception. */
public class AwsDemoException extends RuntimeException {

    /** Creates the exception with just a user-facing message, no underlying cause. */
    public AwsDemoException(String message) {
        super(message);
    }

    /** Creates the exception with a user-facing message plus the lower-level exception that triggered it. */
    public AwsDemoException(String message, Throwable cause) {
        super(message, cause);
    }
}
