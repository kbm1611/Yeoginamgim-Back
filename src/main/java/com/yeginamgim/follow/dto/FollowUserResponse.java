package com.yeginamgim.follow.dto;

import com.yeginamgim.user.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FollowUserResponse {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime followedAt;

    public static FollowUserResponse from(UserEntity user, LocalDateTime followedAt) {
        return FollowUserResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .followedAt(followedAt)
                .build();
    }
}
