package com.yeginamgim.user.controller;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.dto.request.UserWithdrawRequestDto;
import com.yeginamgim.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userSvc;
    private final JWTService jwtSvc;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(@Valid @ModelAttribute UserSignupRequestDto userReqDto) {
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
            @Valid @ModelAttribute UserUpdateRequestDto userUpdDto
    ) {
        String email = jwtSvc.extractEmailFromBearerToken(token);
        return ResponseEntity.ok(userSvc.updateUserInfo(email, userUpdDto));
    }

    // 회원탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<?> withdraw(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody(required = false) UserWithdrawRequestDto request
    ) {
        String email = jwtSvc.extractEmailFromBearerToken(token);
        return ResponseEntity.ok(userSvc.withdraw(email, request));
    }
}
