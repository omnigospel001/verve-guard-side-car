package com.verve.guard.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(
            ConstraintViolationException ex) {

        Map<String, String> errors = new HashMap<>();

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        for (ConstraintViolation<?> violation : violations) {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(field, message);
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> userNotFoundException(UserNotFoundException userNotFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                userNotFoundException.getMessage()
        );
    }

    @ExceptionHandler(RequestCannotBeNullException.class)
    public ResponseEntity<?> requestCannotBeNullException(RequestCannotBeNullException requestCannotBeNullException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                requestCannotBeNullException.getMessage()
        );
    }

    @ExceptionHandler(LowerAmountException.class)
    public ResponseEntity<?> lowerAmountException(LowerAmountException lowerAmountException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                lowerAmountException.getMessage()
        );
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<?> invalidPasswordException(InvalidPasswordException passwordException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                passwordException.getMessage()
        );
    }

    @ExceptionHandler(DuplicateDepositRequestException.class)
    public ResponseEntity<?> invalidPasswordException(DuplicateDepositRequestException depositRequestException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                depositRequestException.getMessage()
        );
    }

    @ExceptionHandler(CannotSendMoneyToYourselfException.class)
    public ResponseEntity<?> cannotSendMoneyToYourselfException(CannotSendMoneyToYourselfException toYourselfException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                toYourselfException.getMessage()
        );
    }

    @ExceptionHandler(AccountNumberNotFoundException.class)
    public ResponseEntity<?> accountNumberNotFoundException(AccountNumberNotFoundException numberNotFoundException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                numberNotFoundException.getMessage()
        );
    }

    @ExceptionHandler(DuplicateTransferRequestException.class)
    public ResponseEntity<?> duplicateTransferRequestException(DuplicateTransferRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                exception.getMessage()
        );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<?> insufficientBalanceException(InsufficientBalanceException requestException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                requestException.getMessage()
        );
    }

    @ExceptionHandler(SuspiciousActivityException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS) // 429
    public ProblemDetail handleSuspiciousActivity(SuspiciousActivityException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        problem.setTitle("Suspicious Activity Detected");
        problem.setDetail(ex.getMessage());
        return problem;
    }


    @ExceptionHandler(YouCannotSendMoneyToYourselfException.class)
    public ResponseEntity<?> youCannotSendMoneyToYourselfException(YouCannotSendMoneyToYourselfException moneyToYourselfException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                moneyToYourselfException.getMessage()
        );
    }

    @ExceptionHandler(ExcessAmountTransferException.class)
    public ProblemDetail excessAmountTransferException(ExcessAmountTransferException excessAmountTransferException) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Excess Amount Transfer Exception");
        problem.setDetail(excessAmountTransferException.getMessage());
        return problem;
    }

}
