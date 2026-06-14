package com.yeginamgim.follow.dto;

import com.yeginamgim.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FollowDtoTest {

    @Test
    void followResponseContainsTargetStateAndCounts() {
        FollowResponse response = FollowResponse.of(2L, true, 5L, 3L);

        assertThat(response.getTargetUserId()).isEqualTo(2L);
        assertThat(response.getFollowing()).isTrue();
        assertThat(response.getFollowerCount()).isEqualTo(5L);
        assertThat(response.getFollowingCount()).isEqualTo(3L);
    }

    @Test
    void followUserResponseContainsPublicUserFields() {
        Instant followedAt = Instant.parse("2026-06-11T10:30:00Z");
        UserEntity user = UserEntity.builder()
                .userId(3L)
                .email("target@example.com")
                .nickname("target")
                .profileImageUrl("https://image.example/profile.png")
                .build();

        FollowUserResponse response = FollowUserResponse.from(user, followedAt);

        assertThat(response.getUserId()).isEqualTo(3L);
        assertThat(response.getNickname()).isEqualTo("target");
        assertThat(response.getProfileImageUrl()).isEqualTo("https://image.example/profile.png");
        assertThat(response.getFollowedAt()).isEqualTo(followedAt);
    }
}
