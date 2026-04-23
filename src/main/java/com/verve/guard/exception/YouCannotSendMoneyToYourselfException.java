package com.verve.guard.exception;

public class YouCannotSendMoneyToYourselfException extends RuntimeException {
    public YouCannotSendMoneyToYourselfException(String message) {
        super(message);
    }
}
