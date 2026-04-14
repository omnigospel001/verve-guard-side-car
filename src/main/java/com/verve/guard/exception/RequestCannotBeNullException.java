package com.verve.guard.exception;

public class RequestCannotBeNullException extends RuntimeException {
    public RequestCannotBeNullException(String message) {
        super(message);
    }
}
