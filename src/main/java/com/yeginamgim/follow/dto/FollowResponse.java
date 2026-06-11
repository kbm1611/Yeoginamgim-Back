package com.yeginamgim.follow.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowResponse {

    private Long targetUserId;
    private Boolean following;
    private Long followerCount;
    private Long followingCount;

    public static FollowResponse of(
            Long targetUserId,
            boolean following,
            long followerCount,
            long followingCount
    ) {
        return FollowResponse.builder()
                .targetUserId(targetUserId)
                .following(following)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }
}
