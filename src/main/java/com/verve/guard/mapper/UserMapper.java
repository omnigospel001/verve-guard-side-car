package com.verve.guard.mapper;

import com.verve.guard.entity.User;
import com.verve.guard.exception.RequestCannotBeNullException;
import com.verve.guard.exception.UserNotFoundException;
import com.verve.guard.request.UserRequest;
import com.verve.guard.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import java.security.SecureRandom;


@Slf4j
@Component
public class UserMapper {

    private static PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User register(UserRequest userRequest) {
        if (userRequest == null)
            throw new RequestCannotBeNullException("Request cannot be empty");

        return User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .accountNumber(generateAccountNumber())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .phone(userRequest.getPhone())
                .merchantId(merchantId())
                .build();
    }

    public static UserResponse updateUser(User user) {
        if (user == null) throw new UserNotFoundException("User is not found");

        return new UserResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getAccountNumber(),
                user.getEmail(),
                user.getPhone()
        );

    }

    public static UserResponse userResponse(User user) {
        if (user == null) throw new UserNotFoundException("User is not found");

        return new UserResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getAccountNumber(),
                user.getEmail(),
                user.getPhone()
        );

    }

    public static long generateAccountNumber() {

        SecureRandom secureRandom = new SecureRandom();

        long upperBound = 1_000_000_000_0L;

        return secureRandom.nextLong(upperBound);

    }

    public static int numbersGenerator(){
        SecureRandom secureRandom = new SecureRandom();
        int upperBound = 1000000; //generates 6 secure random numbers
        return secureRandom.nextInt(upperBound);
    }

    public static String charactersGenerator(){
        int numberOfCharacters = 2;
        String generatedCharacters = RandomStringUtils.randomAlphabetic(numberOfCharacters);
        return generatedCharacters.toUpperCase();
    }

    public static String merchantId(){
        log.info("merchant Id is generated" );
        //Concatenation
        return numbersGenerator() + charactersGenerator();
    }

}