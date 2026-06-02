package com.yeginamgim.auth.dto.response;

import com.yeginamgim.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String token;
    private String email;
    private String nickname;
    private String profileImageUrl;

    // 로그인한 사용자 정보와 발급된 JWT를 로그인 응답 DTO로 변환한다.
    public static LoginResponseDto from(UserEntity userEntity, String token) {
        return LoginResponseDto.builder()
                .token(token)
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .profileImageUrl(userEntity.getProfileImageUrl())
                .build();
    }
}
