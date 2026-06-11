package com.yeginamgim.follow.controller;

import com.yeginamgim.follow.dto.FollowResponse;
import com.yeginamgim.follow.dto.FollowUserResponse;
import com.yeginamgim.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/users/{userId}/follow")
    public FollowResponse follow(
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return followService.follow(userId, authorization);
    }

    @DeleteMapping("/users/{userId}/follow")
    public FollowResponse unfollow(
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return followService.unfollow(userId, authorization);
    }

    @GetMapping("/users/{userId}/followers")
    public List<FollowUserResponse> getFollowers(@PathVariable Long userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/users/{userId}/followings")
    public List<FollowUserResponse> getFollowings(@PathVariable Long userId) {
        return followService.getFollowings(userId);
    }
}
