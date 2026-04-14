package com.verve.guard.exception;

public class DuplicateTransferRequestException extends RuntimeException{
    public DuplicateTransferRequestException(String message) {
        super(message);
    }
}
