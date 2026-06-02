package com.yeginamgim.auth.service;

import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.auth.dto.OAuthUserInfoDto;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
}
