package com.verve.guard.service;

import com.verve.guard.entity.User;
import com.verve.guard.request.LoginRequest;
import com.verve.guard.request.UserRequest;
import com.verve.guard.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    User register(UserRequest userRequest);
}
