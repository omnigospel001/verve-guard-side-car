package com.verve.guard.response;

public record UserResponse(
        String firstName,
        String lastName,
        Long accountNumber,
        String email,
        String phone)
{ }
