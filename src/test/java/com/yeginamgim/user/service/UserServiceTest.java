package com.yeginamgim.user.service;

import com.yeginamgim.auth.service.EmailVerificationRedisService;
import com.yeginamgim.global.exception.AccountWithdrawalException;
import com.yeginamgim.global.exception.InvalidBirthDateException;
import com.yeginamgim.global.exception.DuplicateMemberException;
import com.yeginamgim.global.exception.EmailVerificationException;
import com.yeginamgim.global.exception.FileUploadException;
import com.yeginamgim.global.exception.UserNotFoundException;
import com.yeginamgim.global.file.FileService;
import com.yeginamgim.report.repository.ReportRepository;
import com.yeginamgim.trace.repository.TraceLikeRepository;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.dto.request.UserWithdrawRequestDto;
import com.yeginamgim.user.dto.response.UserSignupResponseDto;
import com.yeginamgim.user.dto.response.UserWithdrawResponseDto;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final FileService fileService = mock(FileService.class);
    private final TraceLikeRepository traceLikeRepository = mock(TraceLikeRepository.class);
    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final EmailVerificationRedisService emailVerificationRedisService = mock(EmailVerificationRedisService.class);
    private final UserService userService = new UserService(
            userRepository,
            fileService,
            traceLikeRepository,
            reportRepository,
            emailVerificationRedisService
    );

    UserServiceTest() {
        when(emailVerificationRedisService.isVerified(any())).thenReturn(true);
    }

    @Test
    void signupResponseDtoDoesNotDeclarePasswordField() {
        assertThat(Arrays.stream(UserSignupResponseDto.class.getDeclaredFields())
                .map(field -> field.getName()))
                .doesNotContain("password");
    }

    @Test
    void userUpdateRequestDtoExposesOnlyEditableProfileFields() {
        assertThat(Arrays.stream(UserUpdateRequestDto.class.getDeclaredFields())
                .map(field -> field.getName()))
                .contains("nickname", "birthDate", "profileUploadFile")
                .doesNotContain("email", "profileImageUrl", "updateAt");
    }

    @Test
    void getMyInfoThrowsUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyInfo("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getMyInfoResponseIncludesUserId() {
        UserEntity existingUser = UserEntity.builder()
                .userId(7L)
                .email("user@example.com")
                .nickname("user")
                .provider(LoginProvider.LOCAL)
                .build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        assertThat(userService.getMyInfo("user@example.com").getUserId()).isEqualTo(7L);
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
    void signupRejectsWhenEmailIsNotVerified() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(emailVerificationRedisService.isVerified("new@example.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessage("이메일 인증이 필요합니다.");

        verify(fileService, never()).profileUpload(any());
        verify(userRepository, never()).save(any());
        verify(emailVerificationRedisService, never()).clearVerificationState(any());
    }

    @Test
    void signupClearsEmailVerificationStateAfterSuccessfulSave() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.signup(request);

        verify(emailVerificationRedisService).clearVerificationState("new@example.com");
    }

    @Test
    void signupRejectsMissingVerifiedStateWithEmailVerificationRequiredCode() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(emailVerificationRedisService.isVerified("new@example.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(EmailVerificationException.class)
                .satisfies(exception -> assertThat(((EmailVerificationException) exception).getCode())
                        .isEqualTo("EMAIL_VERIFICATION_REQUIRED"));

        verify(emailVerificationRedisService).isVerified("new@example.com");
        verify(userRepository, never()).save(any());
        verify(emailVerificationRedisService, never()).clearVerificationState(any());
    }

    @Test
    void signupChecksVerifiedStateBeforeSaveAndClearsItAfterSuccessfulSave() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.signup(request);

        InOrder inOrder = inOrder(userRepository, emailVerificationRedisService);
        inOrder.verify(userRepository).findByEmail("new@example.com");
        inOrder.verify(emailVerificationRedisService).isVerified("new@example.com");
        inOrder.verify(userRepository).save(any(UserEntity.class));
        inOrder.verify(emailVerificationRedisService).clearVerificationState("new@example.com");
    }

    @Test
    void signupDoesNotClearEmailVerificationStateWhenDbSaveFails() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(DuplicateMemberException.class);

        verify(emailVerificationRedisService, never()).clearVerificationState(any());
    }

    @Test
    void signupDoesNotClearEmailVerificationStateWhenProfileUploadFails() {
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
                .profileUploadFile(profileUploadFile)
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        doThrow(FileUploadException.uploadFailed()).when(fileService).profileUpload(profileUploadFile);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(FileUploadException.class);

        verify(userRepository, never()).save(any());
        verify(emailVerificationRedisService, never()).clearVerificationState(any());
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
    void signupDoesNotStoreProfileImageUrlWhenNoUploadFileExists() {
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .nickname("new-user")
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
                .profileUploadFile(profileUploadFile)
                .build();

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(fileService.profileUpload(profileUploadFile)).thenReturn("stored-profile.png");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.signup(request);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getProfileImageUrl()).isEqualTo("stored-profile.png");
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
