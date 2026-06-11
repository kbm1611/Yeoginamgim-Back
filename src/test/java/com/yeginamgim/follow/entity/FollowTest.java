package com.yeginamgim.follow.entity;

import com.yeginamgim.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FollowTest {

    @Test
    void createStoresFollowerAndFollowingUsers() {
        UserEntity follower = user(1L, "follower@example.com");
        UserEntity following = user(2L, "following@example.com");

        Follow follow = Follow.create(follower, following);

        assertThat(follow.getFollower()).isSameAs(follower);
        assertThat(follow.getFollowing()).isSameAs(following);
    }

    private UserEntity user(Long userId, String email) {
        return UserEntity.builder()
                .userId(userId)
                .email(email)
                .nickname("user-" + userId)
                .build();
    }
}
