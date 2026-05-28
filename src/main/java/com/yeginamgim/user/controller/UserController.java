package com.yeginamgim.user.controller;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.dto.response.UserInfoResponseDto;
import com.yeginamgim.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// [1] 회원 정보 관리
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userSvc;
    private final JWTService jwtSvc;
    // (1) 일반 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@ModelAttribute UserSignupRequestDto userReqDto){
        return ResponseEntity.ok(userSvc.signup(userReqDto));
    }
    // (2) 회원 정보 조회, 로그인한 사용자 정보 조회
    @GetMapping("/myinfo")
    public ResponseEntity<?> getMyInfo(@RequestHeader(value = "Authorization") String token){
        if(token == null || !token.startsWith("Bearer")){
            return ResponseEntity.ok( false );
        }
        token = token.replace("Bearer ", "");
        String email = jwtSvc.getClaim(token);
        if(email == null){
            return ResponseEntity.ok( false );
        }
        UserInfoResponseDto result = userSvc.getMyInfo(email);
        if( result == null ){
            return ResponseEntity.status(404).body("회원 정보가 없습니다.");
        }
        return ResponseEntity.ok(result);
    }
    // (3) 회원정보 수정
    @PatchMapping("/update")
    public ResponseEntity<?> updateUserInfo(
            @RequestHeader(value = "Authorization") String token,
            @ModelAttribute UserUpdateRequestDto userUpdDto){

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.ok(false);
        }

        token = token.replace("Bearer ", "");
        String email = jwtSvc.getClaim(token);

        if (email == null) {
            return ResponseEntity.ok(false);
        }

        UserInfoResponseDto result = userSvc.updateUserInfo(email, userUpdDto);

        if (result == null) {
            return ResponseEntity.status(404).body("회원 정보를 찾을 수 없습니다.");
        }

        return ResponseEntity.ok(result);
    }
}
