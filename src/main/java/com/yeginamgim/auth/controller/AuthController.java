package com.yeginamgim.auth.controller;

import com.yeginamgim.auth.dto.request.EmailVerificationSendRequest;
import com.yeginamgim.auth.dto.request.EmailVerificationVerifyRequest;
import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.service.AuthService;
import com.yeginamgim.global.exception.DuplicateMemberException;
import com.yeginamgim.global.exception.OAuthLoginException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authSvc;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    // 일반 로그인 요청을 처리하고 JWT가 포함된 로그인 응답을 반환한다.
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

    // 카카오 OAuth 인증 페이지로 리다이렉트한다.
    @GetMapping("/oauth/kakao")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(authSvc.getKaKaoLoginUrl());
    }

    // 카카오 OAuth callback code로 로그인하거나 가입 처리한 뒤 JWT를 반환한다.
    @GetMapping("/oauth/kakao/callback")
    public void kakaoCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "error", required = false) String error,
            HttpServletResponse response
    ) throws IOException {
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

    // 구글 OAuth 인증 페이지로 리다이렉트한다.
    @GetMapping("/oauth/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(authSvc.getGoogleLoginUrl());
    }

    // 구글 OAuth callback code로 로그인하거나 가입 처리한 뒤 JWT를 반환한다.
    @GetMapping("/oauth/google/callback")
    public void googleCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "error", required = false) String error,
            HttpServletResponse response
    ) throws IOException {
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

    // 클라이언트가 보유한 JWT를 폐기하도록 성공 응답만 반환한다.
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(true);
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
