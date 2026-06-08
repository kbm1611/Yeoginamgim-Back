package com.yeginamgim.auth.controller;

import com.yeginamgim.auth.dto.request.EmailVerificationSendRequest;
import com.yeginamgim.auth.dto.request.EmailVerificationVerifyRequest;
import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.service.AuthService;
import com.yeginamgim.auth.service.OAuthLoginStart;
import com.yeginamgim.global.exception.DuplicateMemberException;
import com.yeginamgim.global.exception.OAuthLoginException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private static final String OAUTH_STATE_COOKIE_NAME = "YEOGINAMGIM_OAUTH_STATE";
    private static final String OAUTH_STATE_COOKIE_PATH = "/api/auth/oauth";
    private static final String OAUTH_STATE_ERROR_MESSAGE = "소셜 로그인 요청이 만료되었습니다. 다시 시도해주세요.";

    private final AuthService authSvc;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.oauth-state-cookie-secure:false}")
    private boolean oauthStateCookieSecure;

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginReqDto) {
        LoginResponseDto result = authSvc.login(loginReqDto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/email/send")
    public ResponseEntity<?> sendEmailVerification(
            @Valid @RequestBody EmailVerificationSendRequest request
    ) {
        return ResponseEntity.ok(authSvc.sendEmailVerification(request));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmailVerification(
            @Valid @RequestBody EmailVerificationVerifyRequest request
    ) {
        return ResponseEntity.ok(authSvc.verifyEmailVerification(request));
    }

    @GetMapping("/oauth/kakao")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        redirectToProvider(response, authSvc.startKakaoOAuth());
    }

    @GetMapping("/oauth/kakao/callback")
    public void kakaoCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "state", required = false) String state,
            @CookieValue(name = OAUTH_STATE_COOKIE_NAME, required = false) String stateCookie,
            HttpServletResponse response
    ) throws IOException {
        if (!isRequestBoundToState(state, stateCookie) || !authSvc.consumeKakaoOAuthState(state)) {
            clearOAuthStateCookie(response);
            redirectToFrontendOAuthCallbackError(response, OAUTH_STATE_ERROR_MESSAGE);
            return;
        }
        clearOAuthStateCookie(response);

        if (hasText(error) || !hasText(code)) {
            redirectToFrontendOAuthCallbackError(response, hasText(error) ? error : "소셜 로그인에 실패했습니다.");
            return;
        }

        try {
            redirectToFrontendOAuthCallback(response, authSvc.kakaoLogin(code));
        } catch (DuplicateMemberException | OAuthLoginException e) {
            redirectToFrontendOAuthCallbackError(response, e.getMessage());
        }
    }

    @GetMapping("/oauth/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        redirectToProvider(response, authSvc.startGoogleOAuth());
    }

    @GetMapping("/oauth/google/callback")
    public void googleCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "state", required = false) String state,
            @CookieValue(name = OAUTH_STATE_COOKIE_NAME, required = false) String stateCookie,
            HttpServletResponse response
    ) throws IOException {
        if (!isRequestBoundToState(state, stateCookie) || !authSvc.consumeGoogleOAuthState(state)) {
            clearOAuthStateCookie(response);
            redirectToFrontendOAuthCallbackError(response, OAUTH_STATE_ERROR_MESSAGE);
            return;
        }
        clearOAuthStateCookie(response);

        if (hasText(error) || !hasText(code)) {
            redirectToFrontendOAuthCallbackError(response, hasText(error) ? error : "소셜 로그인에 실패했습니다.");
            return;
        }

        try {
            redirectToFrontendOAuthCallback(response, authSvc.googleLogin(code));
        } catch (DuplicateMemberException | OAuthLoginException e) {
            redirectToFrontendOAuthCallbackError(response, e.getMessage());
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(true);
    }

    private void redirectToProvider(HttpServletResponse response, OAuthLoginStart loginStart)
            throws IOException {
        addOAuthStateCookie(response, loginStart.state(), loginStart.ttl());
        response.sendRedirect(loginStart.authorizationUrl());
    }

    private boolean isRequestBoundToState(String state, String stateCookie) {
        return hasText(state) && hasText(stateCookie) && state.equals(stateCookie);
    }

    private void addOAuthStateCookie(HttpServletResponse response, String state, Duration maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildOAuthStateCookie(state, maxAge).toString());
    }

    private void clearOAuthStateCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildOAuthStateCookie("", Duration.ZERO).toString());
    }

    private ResponseCookie buildOAuthStateCookie(String value, Duration maxAge) {
        return ResponseCookie.from(OAUTH_STATE_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(oauthStateCookieSecure)
                .sameSite("Lax")
                .path(OAUTH_STATE_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }

    private void redirectToFrontendOAuthCallback(HttpServletResponse response, LoginResponseDto loginResponse)
            throws IOException {
        String baseUrl = normalizeFrontendBaseUrl();
        String token = UriUtils.encode(loginResponse.getToken(), StandardCharsets.UTF_8);
        response.sendRedirect(baseUrl + "/oauth/callback#token=" + token);
    }

    private void redirectToFrontendOAuthCallbackError(HttpServletResponse response, String message)
            throws IOException {
        String baseUrl = normalizeFrontendBaseUrl();
        String encodedMessage = UriUtils.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(baseUrl + "/oauth/callback#error=" + encodedMessage);
    }

    private String normalizeFrontendBaseUrl() {
        return frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
