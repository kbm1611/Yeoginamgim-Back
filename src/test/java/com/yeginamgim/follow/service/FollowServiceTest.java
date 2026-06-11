package com.yeginamgim.follow.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.follow.dto.FollowResponse;
import com.yeginamgim.follow.dto.FollowUserResponse;
import com.yeginamgim.follow.entity.Follow;
import com.yeginamgim.follow.repository.FollowRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FollowServiceTest {

    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final FollowService followService = new FollowService(followRepository, userRepository, jwtService);

    @Test
    void followCreatesRelationAndReturnsCounts() {
        UserEntity follower = user(1L, "follower@example.com", "follower");
        UserEntity following = user(2L, "following@example.com", "following");
        when(jwtService.getClaim("Bearer token")).thenReturn("follower@example.com");
        when(userRepository.findByEmail("follower@example.com")).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollower_UserIdAndFollowing_UserId(1L, 2L)).thenReturn(false);
        when(followRepository.countByFollowing_UserId(2L)).thenReturn(5L);
        when(followRepository.countByFollower_UserId(1L)).thenReturn(3L);

        FollowResponse response = followService.follow(2L, "Bearer token");

        verify(followRepository).save(any(Follow.class));
        assertThat(response.getTargetUserId()).isEqualTo(2L);
        assertThat(response.getFollowing()).isTrue();
        assertThat(response.getFollowerCount()).isEqualTo(5L);
        assertThat(response.getFollowingCount()).isEqualTo(3L);
    }

    @Test
    void followRejectsSelfFollow() {
        UserEntity user = user(1L, "user@example.com", "user");
        when(jwtService.getClaim("Bearer token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> followService.follow(1L, "Bearer token"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(followRepository, never()).save(any());
    }

    @Test
    void getFollowersReturnsUsersFollowingTarget() {
        UserEntity follower = user(3L, "follower@example.com", "follower");
        UserEntity target = user(2L, "target@example.com", "target");
        LocalDateTime followedAt = LocalDateTime.of(2026, 6, 11, 14, 0);
        Follow follow = Follow.builder()
                .followId(10L)
                .follower(follower)
                .following(target)
                .createdAt(followedAt)
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followRepository.findByFollowing_UserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(follow));

        List<FollowUserResponse> responses = followService.getFollowers(2L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(3L);
        assertThat(responses.get(0).getFollowedAt()).isEqualTo(followedAt);
    }

    private UserEntity user(Long userId, String email, String nickname) {
        return UserEntity.builder()
                .userId(userId)
                .email(email)
                .nickname(nickname)
                .build();
    }
}
