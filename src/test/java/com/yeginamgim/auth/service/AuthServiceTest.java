package com.yeginamgim.auth.service;

import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.auth.dto.OAuthUserInfoDto;
import com.yeginamgim.global.exception.DuplicateMemberException;
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

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final KakaoOAuthClientService kakaoOAuthClientService = mock(KakaoOAuthClientService.class);
    private final GoogleOAuthClientService googleOAuthClientService = mock(GoogleOAuthClientService.class);
    private final AuthService authService = new AuthService(
            userRepository,
            jwtService,
            kakaoOAuthClientService,
            googleOAuthClientService
    );

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
