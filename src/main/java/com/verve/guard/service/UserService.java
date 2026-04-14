package com.verve.guard.service;

import com.verve.guard.entity.User;
import com.verve.guard.request.UpdateUserRequest;
import com.verve.guard.request.UserRequest;
import com.verve.guard.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

 UserResponse getCurrentUser();

 List<User> registerMany(List<UserRequest> userRequest);

 UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest);

 UserResponse findById(Long id);

 Page<UserResponse> findAll(Integer page, Integer size);

 void delete(Long id);

}
