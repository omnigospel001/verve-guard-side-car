package com.verve.guard.controller;

import com.verve.guard.entity.User;
import com.verve.guard.request.LoginRequest;
import com.verve.guard.request.UserRequest;
import com.verve.guard.response.LoginResponse;
import com.verve.guard.service.AuthService;
import com.verve.guard.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@Valid @RequestBody UserRequest userRequest) {

        log.error("SIGNUP CALLED >>> {} >>> {}",
                System.currentTimeMillis(),
                userRequest.getEmail());

        return ResponseEntity.ok().body(authService.register(userRequest));
    }


    @PostMapping("/signup/many")
    public ResponseEntity<List<User>> registerMany(@Valid @RequestBody List<UserRequest> userRequest) {

        return ResponseEntity.ok().body(userService.registerMany(userRequest));
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

}
