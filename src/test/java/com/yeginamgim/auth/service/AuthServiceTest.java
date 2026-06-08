package com.yeginamgim.auth.service;

import com.yeginamgim.auth.dto.request.EmailVerificationSendRequest;
import com.yeginamgim.auth.dto.request.EmailVerificationVerifyRequest;
import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.EmailVerificationResponse;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.auth.dto.OAuthUserInfoDto;
import com.yeginamgim.global.exception.DuplicateMemberException;
import com.yeginamgim.global.exception.EmailVerificationException;
import com.yeginamgim.global.exception.EmailVerificationMailException;
import com.yeginamgim.global.exception.LoginFailedException;
import com.yeginamgim.global.exception.OAuthLoginException;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final KakaoOAuthClientService kakaoOAuthClientService = mock(KakaoOAuthClientService.class);
    private final GoogleOAuthClientService googleOAuthClientService = mock(GoogleOAuthClientService.class);
    private final EmailVerificationRedisService emailVerificationRedisService = mock(EmailVerificationRedisService.class);
    private final OAuthStateRedisService oauthStateRedisService = mock(OAuthStateRedisService.class);
    private final MailService mailService = mock(MailService.class);
    private final AuthService authService = new AuthService(
            userRepository,
            jwtService,
            kakaoOAuthClientService,
            googleOAuthClientService,
            emailVerificationRedisService,
            oauthStateRedisService,
            mailService
    );

    @Test
    void sendEmailVerificationRejectsDuplicateEmail() {
        UserEntity existingUser = UserEntity.builder()
                .email("user@example.com")
                .nickname("user")
                .provider(LoginProvider.LOCAL)
                .build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        EmailVerificationSendRequest request = EmailVerificationSendRequest.builder()
                .email(" USER@example.COM ")
                .build();

        assertThatThrownBy(() -> authService.sendEmailVerification(request))
                .isInstanceOf(DuplicateMemberException.class);

        verify(emailVerificationRedisService, never()).storeVerificationCode(any(), any());
    }

    @Test
    void sendEmailVerificationRejectsCooldown() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(emailVerificationRedisService.tryReserveCooldown("user@example.com")).thenReturn(false);
        EmailVerificationSendRequest request = EmailVerificationSendRequest.builder()
                .email(" USER@example.COM ")
                .build();

        assertThatThrownBy(() -> authService.sendEmailVerification(request))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessage("인증번호 재발송은 60초 후에 가능합니다.");
    }

    @Test
    void sendEmailVerificationStoresCodeAndSendsMail() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(emailVerificationRedisService.tryReserveCooldown("user@example.com")).thenReturn(true);
        EmailVerificationSendRequest request = EmailVerificationSendRequest.builder()
                .email(" USER@example.COM ")
                .build();

        EmailVerificationResponse response = authService.sendEmailVerification(request);

        assertThat(response.isVerified()).isFalse();
        assertThat(response.getMessage()).isEqualTo("인증번호가 이메일로 발송되었습니다.");
        verify(emailVerificationRedisService).storeVerificationCode(org.mockito.ArgumentMatchers.eq("user@example.com"), org.mockito.ArgumentMatchers.matches("\\d{6}"));
        verify(mailService).sendVerificationCode(org.mockito.ArgumentMatchers.eq("user@example.com"), org.mockito.ArgumentMatchers.matches("\\d{6}"), org.mockito.ArgumentMatchers.eq(java.time.Duration.ofMinutes(5)));
    }

    @Test
    void sendEmailVerificationClearsVerificationStateWhenMailSendingFails() {
        when(userRepository.findByEmail("user@missing-domain.invalid")).thenReturn(Optional.empty());
        when(emailVerificationRedisService.tryReserveCooldown("user@missing-domain.invalid")).thenReturn(true);
        doThrow(new EmailVerificationMailException(new RuntimeException("missing domain")))
                .when(mailService)
                .sendVerificationCode(
                        org.mockito.ArgumentMatchers.eq("user@missing-domain.invalid"),
                        org.mockito.ArgumentMatchers.matches("\\d{6}"),
                        org.mockito.ArgumentMatchers.eq(java.time.Duration.ofMinutes(5))
                );
        EmailVerificationSendRequest request = EmailVerificationSendRequest.builder()
                .email("user@missing-domain.invalid")
                .build();

        assertThatThrownBy(() -> authService.sendEmailVerification(request))
                .isInstanceOf(EmailVerificationMailException.class);

        verify(emailVerificationRedisService)
                .storeVerificationCode(org.mockito.ArgumentMatchers.eq("user@missing-domain.invalid"), org.mockito.ArgumentMatchers.matches("\\d{6}"));
        verify(emailVerificationRedisService).clearVerificationState("user@missing-domain.invalid");
    }

    @Test
    void verifyEmailVerificationRejectsExpiredCode() {
        when(emailVerificationRedisService.hasVerificationCode("user@example.com")).thenReturn(false);
        EmailVerificationVerifyRequest request = EmailVerificationVerifyRequest.builder()
                .email(" USER@example.COM ")
                .code("123456")
                .build();

        assertThatThrownBy(() -> authService.verifyEmailVerification(request))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessage("인증번호가 만료되었습니다. 다시 요청해 주세요.");
    }

    @Test
    void verifyEmailVerificationRejectsTooManyAttemptsBeforeCheckingCode() {
        when(emailVerificationRedisService.hasVerificationCode("user@example.com")).thenReturn(true);
        when(emailVerificationRedisService.getFailedAttempts("user@example.com")).thenReturn(5L);
        EmailVerificationVerifyRequest request = EmailVerificationVerifyRequest.builder()
                .email("user@example.com")
                .code("123456")
                .build();

        assertThatThrownBy(() -> authService.verifyEmailVerification(request))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessage("인증번호 입력 횟수를 초과했습니다. 다시 요청해 주세요.");

        verify(emailVerificationRedisService, never()).verifyCode(any(), any());
    }

    @Test
    void verifyEmailVerificationRejectsMismatchedCodeAndLimitsAttempts() {
        when(emailVerificationRedisService.hasVerificationCode("user@example.com")).thenReturn(true);
        when(emailVerificationRedisService.getFailedAttempts("user@example.com")).thenReturn(0L, 5L);
        when(emailVerificationRedisService.verifyCode("user@example.com", "000000")).thenReturn(false);
        EmailVerificationVerifyRequest request = EmailVerificationVerifyRequest.builder()
                .email("user@example.com")
                .code("000000")
                .build();

        assertThatThrownBy(() -> authService.verifyEmailVerification(request))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessage("인증번호 입력 횟수를 초과했습니다. 다시 요청해 주세요.");
    }

    @Test
    void verifyEmailVerificationStoresVerifiedStateThroughRedisService() {
        when(emailVerificationRedisService.hasVerificationCode("user@example.com")).thenReturn(true);
        when(emailVerificationRedisService.getFailedAttempts("user@example.com")).thenReturn(0L);
        when(emailVerificationRedisService.verifyCode("user@example.com", "123456")).thenReturn(true);
        EmailVerificationVerifyRequest request = EmailVerificationVerifyRequest.builder()
                .email(" USER@example.COM ")
                .code("123456")
                .build();

        EmailVerificationResponse response = authService.verifyEmailVerification(request);

        assertThat(response.isVerified()).isTrue();
        assertThat(response.getMessage()).isEqualTo("이메일 인증이 완료되었습니다.");
        verify(emailVerificationRedisService).verifyCode("user@example.com", "123456");
    }

    @Test
    void startKakaoOAuthStoresStateAndBuildsAuthorizeUrl() {
        when(oauthStateRedisService.generateState()).thenReturn("state-123");
        when(kakaoOAuthClientService.getLoginUrl("state-123"))
                .thenReturn("https://kauth.kakao.com/oauth/authorize?state=state-123");

        OAuthLoginStart loginStart = authService.startKakaoOAuth();

        assertThat(loginStart.authorizationUrl()).contains("state=state-123");
        assertThat(loginStart.state()).isEqualTo("state-123");
        assertThat(loginStart.ttl()).isEqualTo(java.time.Duration.ofMinutes(5));
        verify(oauthStateRedisService).storeState("state-123", LoginProvider.KAKAO);
    }

    @Test
    void startGoogleOAuthStoresStateAndBuildsAuthorizeUrl() {
        when(oauthStateRedisService.generateState()).thenReturn("state-123");
        when(googleOAuthClientService.getLoginUrl("state-123"))
                .thenReturn("https://accounts.google.com/o/oauth2/v2/auth?state=state-123");

        OAuthLoginStart loginStart = authService.startGoogleOAuth();

        assertThat(loginStart.authorizationUrl()).contains("state=state-123");
        assertThat(loginStart.state()).isEqualTo("state-123");
        assertThat(loginStart.ttl()).isEqualTo(java.time.Duration.ofMinutes(5));
        verify(oauthStateRedisService).storeState("state-123", LoginProvider.GOOGLE);
    }

    @Test
    void consumeOAuthStateRejectsMissingRedisState() {
        when(oauthStateRedisService.findProvider("state-123")).thenReturn(Optional.empty());

        assertThat(authService.consumeKakaoOAuthState("state-123")).isFalse();

        verify(oauthStateRedisService, never()).deleteState(any());
    }

    @Test
    void consumeOAuthStateRejectsProviderMismatch() {
        when(oauthStateRedisService.findProvider("state-123")).thenReturn(Optional.of(LoginProvider.GOOGLE));

        assertThat(authService.consumeKakaoOAuthState("state-123")).isFalse();

        verify(oauthStateRedisService, never()).deleteState(any());
    }

    @Test
    void consumeOAuthStateDeletesStateWhenProviderMatches() {
        when(oauthStateRedisService.findProvider("state-123")).thenReturn(Optional.of(LoginProvider.KAKAO));

        assertThat(authService.consumeKakaoOAuthState("state-123")).isTrue();

        verify(oauthStateRedisService).deleteState("state-123");
    }

    @Test
    void loginRejectsSocialAccountEvenWhenPasswordExists() {
        UserEntity socialUser = UserEntity.builder()
                .email("kakao@example.com")
                .password(new BCryptPasswordEncoder().encode("password"))
                .nickname("kakao-user")
                .provider(LoginProvider.KAKAO)
                .providerId("kakao-provider-id")
                .build();
        LoginRequestDto request = LoginRequestDto.builder()
                .email("kakao@example.com")
                .password("password")
                .build();

        when(userRepository.findByEmail("kakao@example.com")).thenReturn(Optional.of(socialUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    void kakaoLoginUsesOAuthClientAndIssuesToken() {
        OAuthUserInfoDto kakaoUser = new OAuthUserInfoDto(
                LoginProvider.KAKAO,
                "kakao-provider-id",
                "kakao@example.com",
                "kakao-user",
                "/kakao.png"
        );
        UserEntity savedUser = UserEntity.builder()
                .email(kakaoUser.email())
                .nickname(kakaoUser.nickname())
                .profileImageUrl(kakaoUser.profileImageUrl())
                .provider(kakaoUser.provider())
                .providerId(kakaoUser.providerId())
                .build();

        when(kakaoOAuthClientService.fetchUserInfo("callback-code")).thenReturn(kakaoUser);
        when(userRepository.findByProviderAndProviderId(LoginProvider.KAKAO, "kakao-provider-id"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("kakao@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class))).thenReturn(savedUser);
        when(jwtService.createToken("kakao@example.com")).thenReturn("jwt-token");

        LoginResponseDto response = authService.kakaoLogin("callback-code");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("kakao@example.com");
        verify(kakaoOAuthClientService).fetchUserInfo("callback-code");
    }

    @Test
    void googleLoginUsesOAuthClientAndIssuesToken() {
        OAuthUserInfoDto googleUser = new OAuthUserInfoDto(
                LoginProvider.GOOGLE,
                "google-provider-id",
                "google@example.com",
                "google-user",
                "/google.png"
        );
        UserEntity savedUser = UserEntity.builder()
                .email(googleUser.email())
                .nickname(googleUser.nickname())
                .profileImageUrl(googleUser.profileImageUrl())
                .provider(googleUser.provider())
                .providerId(googleUser.providerId())
                .build();

        when(googleOAuthClientService.fetchUserInfo("callback-code")).thenReturn(googleUser);
        when(userRepository.findByProviderAndProviderId(LoginProvider.GOOGLE, "google-provider-id"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class))).thenReturn(savedUser);
        when(jwtService.createToken("google@example.com")).thenReturn("jwt-token");

        LoginResponseDto response = authService.googleLogin("callback-code");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("google@example.com");
        verify(googleOAuthClientService).fetchUserInfo("callback-code");
    }

    @Test
    void socialLoginRejectsDuplicateEmailWithoutAutoLinking() {
        OAuthUserInfoDto kakaoUser = new OAuthUserInfoDto(
                LoginProvider.KAKAO,
                "kakao-provider-id",
                "user@example.com",
                "kakao-user",
                "/kakao.png"
        );
        UserEntity existingUser = UserEntity.builder()
                .email("user@example.com")
                .nickname("local-user")
                .provider(LoginProvider.LOCAL)
                .build();

        when(kakaoOAuthClientService.fetchUserInfo("callback-code")).thenReturn(kakaoUser);
        when(userRepository.findByProviderAndProviderId(LoginProvider.KAKAO, "kakao-provider-id"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.kakaoLogin("callback-code"))
                .isInstanceOf(DuplicateMemberException.class);

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void socialLoginRejectsWithdrawnOAuthAccount() {
        OAuthUserInfoDto kakaoUser = new OAuthUserInfoDto(
                LoginProvider.KAKAO,
                "kakao-provider-id",
                "kakao@example.com",
                "kakao-user",
                "/kakao.png"
        );
        UserEntity withdrawnUser = UserEntity.builder()
                .userId(9L)
                .email("kakao@example.com")
                .nickname("kakao-user")
                .provider(LoginProvider.KAKAO)
                .providerId("kakao-provider-id")
                .build();
        withdrawnUser.withdraw();

        when(kakaoOAuthClientService.fetchUserInfo("callback-code")).thenReturn(kakaoUser);
        when(userRepository.findByProviderAndProviderId(LoginProvider.KAKAO, "kakao-provider-id"))
                .thenReturn(Optional.of(withdrawnUser));

        assertThatThrownBy(() -> authService.kakaoLogin("callback-code"))
                .isInstanceOf(OAuthLoginException.class);

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(jwtService, never()).createToken(any());
    }
}
