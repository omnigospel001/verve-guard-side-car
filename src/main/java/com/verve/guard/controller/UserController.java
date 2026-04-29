package com.verve.guard.controller;

import com.verve.guard.request.UpdateUserRequest;
import com.verve.guard.response.UserResponse;
import com.verve.guard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest updateUserRequest,
                                                    Authentication currentUser) {

        return ResponseEntity.ok().body(userService.updateUser(currentUser, updateUserRequest));
    }

    @GetMapping("/find")
    public ResponseEntity<UserResponse> findById(Authentication currentUser) {

        return ResponseEntity.ok().body(userService.findById(currentUser));
    }


    @GetMapping("/all")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<UserResponse> users = userService.findAll(page, size);

        return ResponseEntity.ok(users);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(Authentication currentUser) {
        userService.delete(currentUser);
        return ResponseEntity.ok().build();
    }

}
