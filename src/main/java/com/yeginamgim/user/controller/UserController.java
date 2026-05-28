package com.yeginamgim.user.controller;

import com.yeginamgim.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// [1] 회원 정보 관리
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userSvc;
    // (1) 회원 정보 조회, 로그인한 사용자 정보 조회
    // (2) 닉네임 수정
    // (3) 프로필 이미지 조회
    // (4) 사용자 권한 관리
    // (5) 사용자가 남긴 포스트잇과 연결
}
