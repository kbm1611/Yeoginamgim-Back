package com.yeginamgim.user.dto.response;

import com.yeginamgim.user.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSearchResponseDto {

    private Long userId;
    private String nickname;
    private String profileImageUrl;

    public static UserSearchResponseDto from(UserEntity user) {
        return UserSearchResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
