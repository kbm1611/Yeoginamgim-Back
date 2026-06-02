package com.yeginamgim.user.service;

import com.yeginamgim.global.exception.UserNotFoundException;
import com.yeginamgim.global.file.FileService;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final FileService fileService = mock(FileService.class);
    private final UserService userService = new UserService(userRepository, fileService);

    @Test
    void getMyInfoThrowsUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyInfo("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUserInfoThrowsUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserInfo(
                "missing@example.com",
                UserUpdateRequestDto.builder().nickname("new-name").build()
        )).isInstanceOf(UserNotFoundException.class);
    }
}
