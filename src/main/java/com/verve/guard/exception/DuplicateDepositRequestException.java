package com.verve.guard.exception;

public class DuplicateDepositRequestException extends RuntimeException {
    public DuplicateDepositRequestException(String message) {
        super(message);
    }
}
