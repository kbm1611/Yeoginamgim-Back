package com.yeginamgim.auth.controller;

import com.yeginamgim.user.service.UserService;import lombok.RequiredArgsConstructor;import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userSvc;
    // 일반 로그인
    // 카카오 OAuth 로그인
    // 구글 Oauth 로그인
    // 로그아웃
}
