package com.yeginamgim.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 회원가입 응답 객체 Dto
public class UserSignupResponseDto {
    private String email;
    private String password;
    private String nickname;
    private String profileImageUrl;
    private String birthDate;
    private LocalDateTime createdAt;
}
