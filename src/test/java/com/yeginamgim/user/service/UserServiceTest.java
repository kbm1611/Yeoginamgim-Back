package com.yeginamgim.user.service;

import com.yeginamgim.global.exception.InvalidBirthDateException;
import com.yeginamgim.global.exception.UserNotFoundException;
import com.yeginamgim.global.file.FileService;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    @Test
    void signupStoresValidBirthDate() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .birthDate("060615")
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.signup(request);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getBirthDate()).isEqualTo("060615");
    }

    @Test
    void signupRejectsInvalidActualBirthDate() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .birthDate("990230")
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(InvalidBirthDateException.class);
    }

    @Test
    void updateUserInfoStoresValidBirthDate() {
        UserEntity existingUser = UserEntity.builder()
                .email("user@example.com")
                .nickname("old-name")
                .birthDate(null)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        userService.updateUserInfo(
                "user@example.com",
                UserUpdateRequestDto.builder().birthDate("990101").build()
        );

        assertThat(existingUser.getBirthDate()).isEqualTo("990101");
    }

    @Test
    void updateUserInfoRejectsInvalidActualBirthDate() {
        UserEntity existingUser = UserEntity.builder()
                .email("user@example.com")
                .nickname("old-name")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.updateUserInfo(
                "user@example.com",
                UserUpdateRequestDto.builder().birthDate("991332").build()
        )).isInstanceOf(InvalidBirthDateException.class);
    }
}
