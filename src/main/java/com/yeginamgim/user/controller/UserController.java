package com.yeginamgim.user.controller;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userSvc;
    private final JWTService jwtSvc;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@ModelAttribute UserSignupRequestDto userReqDto) {
        return ResponseEntity.ok(userSvc.signup(userReqDto));
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> getMyInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        String email = jwtSvc.extractEmailFromBearerToken(token);
        return ResponseEntity.ok(userSvc.getMyInfo(email));
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateUserInfo(
            @RequestHeader(value = "Authorization", required = false) String token,
            @ModelAttribute UserUpdateRequestDto userUpdDto
    ) {
        String email = jwtSvc.extractEmailFromBearerToken(token);
        return ResponseEntity.ok(userSvc.updateUserInfo(email, userUpdDto));
    }
}
