package com.verve.guard.service;

import com.verve.guard.entity.User;
import com.verve.guard.request.UpdateUserRequest;
import com.verve.guard.request.UserRequest;
import com.verve.guard.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {

 List<User> registerMany(List<UserRequest> userRequest);

 UserResponse updateUser(Authentication currentUser, UpdateUserRequest updateUserRequest);

 UserResponse findById(Authentication currentUser);

 Page<UserResponse> findAll(Integer page, Integer size);

 void delete(Authentication currentUser);

}
