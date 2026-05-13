package com.verve.guard.exception;

public class ExcessAmountTransferException extends RuntimeException {
    public ExcessAmountTransferException(String message) {
        super(message);
    }
}
