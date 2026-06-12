package com.yeginamgim.follow.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.follow.dto.FollowResponse;
import com.yeginamgim.follow.dto.FollowUserResponse;
import com.yeginamgim.follow.entity.Follow;
import com.yeginamgim.follow.repository.FollowRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    @Transactional
    public FollowResponse follow(Long targetUserId, String authorization) {
        UserEntity follower = findUserByToken(authorization);
        validateNotSelf(follower.getUserId(), targetUserId);
        UserEntity following = findActiveUser(targetUserId);

        if (!followRepository.existsByFollower_UserIdAndFollowing_UserId(
                follower.getUserId(), following.getUserId())) {
            followRepository.save(Follow.create(follower, following));
        }

        return toFollowResponse(follower.getUserId(), following.getUserId(), true);
    }

    @Transactional
    public FollowResponse unfollow(Long targetUserId, String authorization) {
        UserEntity follower = findUserByToken(authorization);
        validateNotSelf(follower.getUserId(), targetUserId);
        UserEntity following = findActiveUser(targetUserId);

        followRepository.deleteByFollower_UserIdAndFollowing_UserId(follower.getUserId(), following.getUserId());

        return toFollowResponse(follower.getUserId(), following.getUserId(), false);
    }

    @Transactional(readOnly = true)
    public FollowResponse getFollowStatus(Long targetUserId, String authorization) {
        UserEntity follower = findUserByToken(authorization);
        validateNotSelf(follower.getUserId(), targetUserId);
        UserEntity following = findActiveUser(targetUserId);
        boolean followingStatus = followRepository.existsByFollower_UserIdAndFollowing_UserId(
                follower.getUserId(), following.getUserId());

        return toFollowResponse(follower.getUserId(), following.getUserId(), followingStatus);
    }

    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowers(Long userId) {
        findActiveUser(userId);

        return followRepository.findByFollowing_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(follow -> FollowUserResponse.from(follow.getFollower(), follow.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowings(Long userId) {
        findActiveUser(userId);

        return followRepository.findByFollower_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(follow -> FollowUserResponse.from(follow.getFollowing(), follow.getCreatedAt()))
                .toList();
    }

    private FollowResponse toFollowResponse(Long followerId, Long followingId, boolean following) {
        return FollowResponse.of(
                followingId,
                following,
                followRepository.countByFollowing_UserId(followingId),
                followRepository.countByFollower_UserId(followerId)
        );
    }

    private void validateNotSelf(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot follow yourself.");
        }
    }

    private UserEntity findUserByToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized request.");
        }

        String email = jwtService.getClaim(authorization);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized request.");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }

    private UserEntity findActiveUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> !user.isWithdrawn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
