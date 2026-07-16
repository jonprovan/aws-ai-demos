package com.skillstorm.awsdemos.exception;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;

/** Converts AWS SDK / demo exceptions into a message safe to show on the page. */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static String of(Exception e) {
        if (e instanceof AwsServiceException ase) {
            return "AWS rejected the request (%s): %s"
                    .formatted(ase.awsErrorDetails().errorCode(), ase.awsErrorDetails().errorMessage());
        }
        if (e instanceof SdkClientException) {
            return "Could not reach AWS: " + e.getMessage();
        }
        if (e instanceof AwsDemoException) {
            return e.getMessage();
        }
        return "Unexpected error: " + e.getMessage();
    }
}
