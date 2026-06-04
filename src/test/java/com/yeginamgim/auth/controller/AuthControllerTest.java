package com.yeginamgim.auth.controller;

import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController authController = new AuthController(authService);

    @Test
    void kakaoCallbackRedirectsToFrontendCallbackWithToken() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173");
        when(authService.kakaoLogin("callback-code")).thenReturn(LoginResponseDto.builder()
                .token("jwt-token")
                .email("kakao@example.com")
                .nickname("kakao-user")
                .build());
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.kakaoCallback("callback-code", response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/oauth/callback#token=jwt-token");
        verify(authService).kakaoLogin("callback-code");
    }

    @Test
    void googleCallbackRedirectsToFrontendCallbackWithToken() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173/");
        when(authService.googleLogin("callback-code")).thenReturn(LoginResponseDto.builder()
                .token("jwt-token")
                .email("google@example.com")
                .nickname("google-user")
                .build());
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.googleCallback("callback-code", response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/oauth/callback#token=jwt-token");
        verify(authService).googleLogin("callback-code");
    }
}
