package com.yeginamgim.user.service;

import com.yeginamgim.global.exception.AccountWithdrawalException;
import com.yeginamgim.global.exception.InvalidBirthDateException;
import com.yeginamgim.global.exception.UserNotFoundException;
import com.yeginamgim.global.file.FileService;
import com.yeginamgim.report.repository.ReportRepository;
import com.yeginamgim.trace.repository.TraceLikeRepository;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.dto.request.UserWithdrawRequestDto;
import com.yeginamgim.user.dto.response.UserWithdrawResponseDto;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;

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
    private final TraceLikeRepository traceLikeRepository = mock(TraceLikeRepository.class);
    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final UserService userService = new UserService(
            userRepository,
            fileService,
            traceLikeRepository,
            reportRepository
    );

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
    void signupResponseIncludesBirthDate() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .birthDate("060615")
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(userService.signup(request).getBirthDate()).isEqualTo("060615");
    }

    @Test
    void signupIgnoresProfileImageUrlFromRequestWhenNoUploadFileExists() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .profileImageUrl("https://attacker.example/profile.png")
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.signup(request);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getProfileImageUrl()).isNull();
    }

    @Test
    void signupStoresOnlyUploadedProfileImageUrlWhenUploadFileExists() {
        MockMultipartFile profileUploadFile = new MockMultipartFile(
                "profileUploadFile",
                "profile.png",
                "image/png",
                "image".getBytes()
        );
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .profileImageUrl("https://attacker.example/profile.png")
                .profileUploadFile(profileUploadFile)
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(fileService.profileUpload(profileUploadFile)).thenReturn("stored-profile.png");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.signup(request);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getProfileImageUrl()).isEqualTo("/upload/profile/stored-profile.png");
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
    void updateUserInfoIgnoresProfileImageUrlFromRequestWhenNoUploadFileExists() {
        UserEntity existingUser = UserEntity.builder()
                .email("user@example.com")
                .nickname("old-name")
                .profileImageUrl("/upload/profile/current.png")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        userService.updateUserInfo(
                "user@example.com",
                UserUpdateRequestDto.builder()
                        .profileImageUrl("https://attacker.example/profile.png")
                        .build()
        );

        assertThat(existingUser.getProfileImageUrl()).isEqualTo("/upload/profile/current.png");
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

    @Test
    void withdrawLocalUserRequiresPasswordAndSoftDeletesAccount() {
        UserEntity existingUser = UserEntity.builder()
                .userId(7L)
                .email("user@example.com")
                .password(new BCryptPasswordEncoder().encode("password123"))
                .nickname("old-name")
                .profileImageUrl("/upload/profile/me.png")
                .birthDate("990101")
                .provider(LoginProvider.LOCAL)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        UserWithdrawResponseDto response = userService.withdraw(
                "user@example.com",
                UserWithdrawRequestDto.builder().password("password123").build()
        );

        assertThat(response.isWithdrawn()).isTrue();
        assertThat(response.getWithdrawnAt()).isNotNull();
        assertThat(existingUser.getDeletedAt()).isNotNull();
        assertThat(existingUser.getEmail()).startsWith("withdrawn-7-");
        assertThat(existingUser.getPassword()).isNull();
        assertThat(existingUser.getNickname()).isEqualTo("탈퇴한 사용자");
        assertThat(existingUser.getProfileImageUrl()).isNull();
        assertThat(existingUser.getBirthDate()).isNull();
        verify(traceLikeRepository).deleteByUser_UserId(7L);
        verify(reportRepository).deleteByUser_UserId(7L);
    }

    @Test
    void withdrawLocalUserRejectsWrongPassword() {
        UserEntity existingUser = UserEntity.builder()
                .userId(7L)
                .email("user@example.com")
                .password(new BCryptPasswordEncoder().encode("password123"))
                .nickname("old-name")
                .provider(LoginProvider.LOCAL)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.withdraw(
                "user@example.com",
                UserWithdrawRequestDto.builder().password("wrong-password").build()
        )).isInstanceOf(AccountWithdrawalException.class);
    }

    @Test
    void withdrawOAuthUserRequiresConfirmationPhraseInsteadOfPassword() {
        UserEntity existingUser = UserEntity.builder()
                .userId(8L)
                .email("oauth@example.com")
                .password(null)
                .nickname("oauth-user")
                .provider(LoginProvider.KAKAO)
                .providerId("kakao-id")
                .build();

        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.of(existingUser));

        UserWithdrawResponseDto response = userService.withdraw(
                "oauth@example.com",
                UserWithdrawRequestDto.builder().confirmation("회원탈퇴").build()
        );

        assertThat(response.isWithdrawn()).isTrue();
        assertThat(existingUser.getProviderId()).isEqualTo("kakao-id");
        verify(traceLikeRepository).deleteByUser_UserId(8L);
        verify(reportRepository).deleteByUser_UserId(8L);
    }
}
