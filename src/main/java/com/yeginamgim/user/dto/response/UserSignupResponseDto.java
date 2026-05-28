package com.yeginamgim.user.dto.response;

import com.yeginamgim.user.enums.LoginProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 회원가입 응답에 대한 Dto
public class UserSignupResponseDto {
    private Long userId;
    private String email;
    private String nickname;
    private LoginProvider provider;
}
