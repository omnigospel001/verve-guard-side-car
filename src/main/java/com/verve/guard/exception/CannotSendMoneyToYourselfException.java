package com.verve.guard.exception;

public class CannotSendMoneyToYourselfException extends RuntimeException {
    public CannotSendMoneyToYourselfException(String message) {
        super(message);
    }
}
