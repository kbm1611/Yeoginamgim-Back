package com.yeginamgim.auth.controller;

import com.yeginamgim.auth.dto.request.EmailVerificationSendRequest;
import com.yeginamgim.auth.dto.request.EmailVerificationVerifyRequest;
import com.yeginamgim.auth.dto.response.EmailVerificationResponse;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.service.AuthService;
import com.yeginamgim.auth.service.OAuthLoginStart;
import com.yeginamgim.global.exception.DuplicateMemberException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController authController = new AuthController(authService);

    @Test
    void sendEmailVerificationDelegatesToAuthService() {
        EmailVerificationSendRequest request = EmailVerificationSendRequest.builder()
                .email("user@example.com")
                .build();
        EmailVerificationResponse serviceResponse = EmailVerificationResponse.builder()
                .message("verification code sent")
                .verified(false)
                .build();
        when(authService.sendEmailVerification(request)).thenReturn(serviceResponse);

        ResponseEntity<?> response = authController.sendEmailVerification(request);

        assertThat(response.getBody()).isEqualTo(serviceResponse);
        verify(authService).sendEmailVerification(request);
    }

    @Test
    void verifyEmailVerificationDelegatesToAuthService() {
        EmailVerificationVerifyRequest request = EmailVerificationVerifyRequest.builder()
                .email("user@example.com")
                .code("123456")
                .build();
        EmailVerificationResponse serviceResponse = EmailVerificationResponse.builder()
                .message("email verified")
                .verified(true)
                .build();
        when(authService.verifyEmailVerification(request)).thenReturn(serviceResponse);

        ResponseEntity<?> response = authController.verifyEmailVerification(request);

        assertThat(response.getBody()).isEqualTo(serviceResponse);
        verify(authService).verifyEmailVerification(request);
    }

    @Test
    void kakaoLoginRedirectsToAuthorizeUrlWithStateCookie() throws Exception {
        when(authService.startKakaoOAuth()).thenReturn(new OAuthLoginStart(
                "https://kauth.kakao.com/oauth/authorize?state=state-123",
                "state-123",
                Duration.ofMinutes(5)
        ));
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.kakaoLogin(response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).contains("state=state-123");
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> {
                    assertThat(cookie).contains("YEOGINAMGIM_OAUTH_STATE=state-123");
                    assertThat(cookie).contains("HttpOnly");
                    assertThat(cookie).contains("SameSite=Lax");
                    assertThat(cookie).contains("Path=/api/auth/oauth");
                    assertThat(cookie).contains("Max-Age=300");
                });
    }

    @Test
    void googleLoginRedirectsToAuthorizeUrlWithStateCookie() throws Exception {
        when(authService.startGoogleOAuth()).thenReturn(new OAuthLoginStart(
                "https://accounts.google.com/o/oauth2/v2/auth?state=state-123",
                "state-123",
                Duration.ofMinutes(5)
        ));
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.googleLogin(response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).contains("state=state-123");
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> assertThat(cookie).contains("YEOGINAMGIM_OAUTH_STATE=state-123"));
    }

    @Test
    void kakaoCallbackRedirectsToFrontendCallbackWithTokenAndClearsStateCookie() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173");
        when(authService.consumeKakaoOAuthState("state-123")).thenReturn(true);
        when(authService.kakaoLogin("callback-code")).thenReturn(LoginResponseDto.builder()
                .token("jwt-token")
                .email("kakao@example.com")
                .nickname("kakao-user")
                .build());
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.kakaoCallback("callback-code", null, "state-123", "state-123", response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/oauth/callback#token=jwt-token");
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> {
                    assertThat(cookie).contains("YEOGINAMGIM_OAUTH_STATE=");
                    assertThat(cookie).contains("Max-Age=0");
                });
        verify(authService).consumeKakaoOAuthState("state-123");
        verify(authService).kakaoLogin("callback-code");
    }

    @Test
    void googleCallbackRedirectsToFrontendCallbackWithToken() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173/");
        when(authService.consumeGoogleOAuthState("state-123")).thenReturn(true);
        when(authService.googleLogin("callback-code")).thenReturn(LoginResponseDto.builder()
                .token("jwt-token")
                .email("google@example.com")
                .nickname("google-user")
                .build());
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.googleCallback("callback-code", null, "state-123", "state-123", response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/oauth/callback#token=jwt-token");
        verify(authService).consumeGoogleOAuthState("state-123");
        verify(authService).googleLogin("callback-code");
    }

    @Test
    void kakaoCallbackRedirectsToFrontendCallbackWithProviderErrorWithoutCode() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173");
        when(authService.consumeKakaoOAuthState("state-123")).thenReturn(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.kakaoCallback(null, "access_denied", "state-123", "state-123", response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/oauth/callback#error=access_denied");
        verify(authService, never()).kakaoLogin(any());
    }

    @Test
    void googleCallbackRedirectsToFrontendCallbackWithEncodedServiceError() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173");
        when(authService.consumeGoogleOAuthState("state-123")).thenReturn(true);
        when(authService.googleLogin("callback-code")).thenThrow(new DuplicateMemberException());
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.googleCallback("callback-code", null, "state-123", "state-123", response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).startsWith("http://localhost:5173/oauth/callback#error=");
        assertThat(response.getRedirectedUrl()).contains("%EC%9D%B4%EB%AF%B8");
        verify(authService).googleLogin("callback-code");
    }

    @Test
    void kakaoCallbackRedirectsToErrorWhenStateParamIsMissing() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.kakaoCallback("callback-code", null, null, "state-123", response);

        assertThat(response.getRedirectedUrl()).startsWith("http://localhost:5173/oauth/callback#error=");
        verify(authService, never()).kakaoLogin(any());
    }

    @Test
    void kakaoCallbackRedirectsToErrorWhenStateCookieIsMissing() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.kakaoCallback("callback-code", null, "state-123", null, response);

        assertThat(response.getRedirectedUrl()).startsWith("http://localhost:5173/oauth/callback#error=");
        verify(authService, never()).kakaoLogin(any());
    }

    @Test
    void kakaoCallbackRedirectsToErrorWhenStateParamAndCookieDoNotMatch() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.kakaoCallback("callback-code", null, "state-123", "other-state", response);

        assertThat(response.getRedirectedUrl()).startsWith("http://localhost:5173/oauth/callback#error=");
        verify(authService, never()).kakaoLogin(any());
    }

    @Test
    void kakaoCallbackRedirectsToErrorWhenRedisStateIsMissingOrExpired() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173");
        when(authService.consumeKakaoOAuthState("state-123")).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.kakaoCallback("callback-code", null, "state-123", "state-123", response);

        assertThat(response.getRedirectedUrl()).startsWith("http://localhost:5173/oauth/callback#error=");
        verify(authService).consumeKakaoOAuthState("state-123");
        verify(authService, never()).kakaoLogin(any());
    }

    @Test
    void googleCallbackUsesSameStatePolicyAsKakao() throws Exception {
        ReflectionTestUtils.setField(authController, "frontendBaseUrl", "http://localhost:5173");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authController.googleCallback("callback-code", null, "state-123", "other-state", response);

        assertThat(response.getRedirectedUrl()).startsWith("http://localhost:5173/oauth/callback#error=");
        verify(authService, never()).googleLogin(any());
    }
}
