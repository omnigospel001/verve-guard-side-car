package com.verve.guard.serviceImpl;

import com.verve.guard.entity.User;
import com.verve.guard.exception.UserNotFoundException;
import com.verve.guard.mapper.UserMapper;
import com.verve.guard.repository.UserRepository;
import com.verve.guard.request.UpdateUserRequest;
import com.verve.guard.request.UserRequest;
import com.verve.guard.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequest userRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@gmail.com");
        user.setPhone("08012345678");

        userRequest = new UserRequest();
        userRequest.setFirstName("John");
        userRequest.setLastName("Doe");
        userRequest.setEmail("john@gmail.com");

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("Updated");
        updateUserRequest.setLastName("User");
        updateUserRequest.setEmail("updated@gmail.com");
        updateUserRequest.setPhone("09098765432");
    }

    @Test
    void registerMany_success() {

        List<UserRequest> requests = List.of(userRequest);

        when(userMapper.register(any(UserRequest.class))).thenReturn(user);
        when(userRepository.saveAll(anyList())).thenReturn(List.of(user));

        List<User> result = userService.registerMany(requests);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userMapper, times(1)).register(any(UserRequest.class));
        verify(userRepository, times(1)).saveAll(anyList());
    }

    @Test
    void updateUser_success() {

        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        try (MockedStatic<UserMapper> mockedStatic = mockStatic(UserMapper.class)) {

            UserResponse response = new UserResponse(
                    "Updated",
                    "User",
                    1234567890L,
                    "updated@gmail.com",
                    "09098765432"
            );

            mockedStatic.when(() -> UserMapper.updateUser(any(User.class)))
                    .thenReturn(response);

            UserResponse result = userService.updateUser(authentication, updateUserRequest);

            assertNotNull(result);
            assertEquals("Updated", result.firstName());
            verify(userRepository).findById(1L);
        }
    }

    @Test
    void updateUser_userNotFound() {

        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(authentication, updateUserRequest));

        verify(userRepository).findById(1L);
    }


    @Test
    void findById_userNotFound() {

        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.findById(authentication));

        verify(userRepository).findById(1L);
    }

    @Test
    void findAll_success() {

        Pageable pageable = PageRequest.of(0, 5);

        Page<User> userPage = new PageImpl<>(
                List.of(user),
                pageable,
                1
        );

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        try (MockedStatic<UserMapper> mockedStatic = mockStatic(UserMapper.class)) {

            UserResponse response = new UserResponse(
                    "Updated",
                    "User",
                    1234567890L,
                    "updated@gmail.com",
                    "09098765432"
            );

            mockedStatic.when(() -> UserMapper.userResponse(any(User.class)))
                    .thenReturn(response);

            Page<UserResponse> result = userService.findAll(0, 5);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            verify(userRepository).findAll(pageable);
        }
    }

    @Test
    void delete_success() {

        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(authentication);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void delete_userNotFound() {

        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.delete(authentication));

        verify(userRepository).findById(1L);
    }
}