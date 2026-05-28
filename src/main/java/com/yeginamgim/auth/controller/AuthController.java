package com.yeginamgim.auth.controller;

import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    // 구글 Oauth 로그인
    // 로그아웃
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(true);
    }
}
