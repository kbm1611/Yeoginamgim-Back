package com.yeginamgim.auth.controller;

import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authSvc;

    // 일반 로그인
    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginReqDto){
        LoginResponseDto result = authSvc.login(loginReqDto);

        if( result == null ){
            return ResponseEntity.status(401).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return ResponseEntity.ok(result);
    }

    // 카카오 OAuth 로그인
    @GetMapping("/oauth/kakao")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(authSvc.getKaKaoLoginUrl());
    }
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code){
        return ResponseEntity.ok(authSvc.kakaoLogin(code));
    }

    // 구글 Oauth 로그인
    @GetMapping("/oauth/google")
    public void googleLogin(HttpServletResponse response) throws IOException{
        response.sendRedirect(authSvc.getGoogleLoginUrl());
    }
    @GetMapping("/oauth/google/callback")
    public ResponseEntity<?> googleCallback(@RequestParam String code){
        return ResponseEntity.ok(authSvc.googleLogin(code));
    }

    // 로그아웃
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(true);
    }
}
