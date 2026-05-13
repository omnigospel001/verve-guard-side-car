package com.verve.guard.serviceImpl;


import com.verve.guard.entity.User;
import com.verve.guard.exception.UserNotFoundException;
import com.verve.guard.mapper.UserMapper;
import com.verve.guard.repository.UserRepository;
import com.verve.guard.request.UpdateUserRequest;
import com.verve.guard.request.UserRequest;
import com.verve.guard.response.UserResponse;
import com.verve.guard.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    //
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    @Override
    public List<User> registerMany(List<UserRequest> userRequests) {

        List<User> users = userRequests.stream()
                .map(userMapper::register)
                .toList();

        return userRepository.saveAll(users);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#currentUser.principal.id")
    public UserResponse updateUser(Authentication currentUser, UpdateUserRequest updateUserRequest) {

        User user = (User) currentUser.getPrincipal();

        User userFound = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("User not found"));

        //
        userFound.setFirstName(updateUserRequest.getFirstName());
        userFound.setLastName(updateUserRequest.getLastName());
        userFound.setEmail(updateUserRequest.getEmail());
        userFound.setPhone(updateUserRequest.getPhone());

        return UserMapper.updateUser(userFound);
    }

    @Override
    @Transactional
    @Cacheable(value = "users", key = "#currentUser.principal.id")
    public UserResponse findById(Authentication currentUser) {

        User user = (User) currentUser.getPrincipal();

        User userFound = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserMapper.userResponse(userFound);
    }

    @Override
    @Transactional
    @Cacheable(value = "users", key = "'all_' + #page + '_' + #size")
    public Page<UserResponse> findAll(Integer page, Integer size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findAll(pageable);

        return users.map(UserMapper::userResponse);

    }

    @Override
    @CacheEvict(value = "users", key = "#currentUser.principal.id")
    public void delete(Authentication currentUser) {

        User user = (User) currentUser.getPrincipal();

        User userFound = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("No user not available to delete"));

        userRepository.delete(userFound);
        log.info("User with id: {} is deleted", id );
    }

}
