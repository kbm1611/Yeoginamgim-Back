package com.yeginamgim.follow.controller;

import com.yeginamgim.follow.dto.FollowResponse;
import com.yeginamgim.follow.dto.FollowUserResponse;
import com.yeginamgim.follow.service.FollowService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class FollowControllerTest {

    private final FollowService followService = mock(FollowService.class);
    private final MockMvc mockMvc = standaloneSetup(new FollowController(followService)).build();

    @Test
    void followEndpointDelegatesAuthorizationHeader() throws Exception {
        when(followService.follow(2L, "Bearer token"))
                .thenReturn(FollowResponse.of(2L, true, 5L, 3L));

        mockMvc.perform(post("/api/users/2/follow")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetUserId").value(2))
                .andExpect(jsonPath("$.following").value(true))
                .andExpect(jsonPath("$.followerCount").value(5))
                .andExpect(jsonPath("$.followingCount").value(3));

        verify(followService).follow(2L, "Bearer token");
    }

    @Test
    void unfollowEndpointDelegatesAuthorizationHeader() throws Exception {
        when(followService.unfollow(2L, "Bearer token"))
                .thenReturn(FollowResponse.of(2L, false, 4L, 2L));

        mockMvc.perform(delete("/api/users/2/follow")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));

        verify(followService).unfollow(2L, "Bearer token");
    }

    @Test
    void followerListEndpointReturnsUsers() throws Exception {
        when(followService.getFollowers(2L)).thenReturn(List.of(
                FollowUserResponse.builder()
                        .userId(1L)
                        .nickname("follower")
                        .profileImageUrl("image")
                        .followedAt(LocalDateTime.of(2026, 6, 11, 15, 0))
                        .build()
        ));

        mockMvc.perform(get("/api/users/2/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].nickname").value("follower"));

        verify(followService).getFollowers(2L);
    }

    @Test
    void followStatusEndpointDelegatesAuthorizationHeader() throws Exception {
        when(followService.getFollowStatus(2L, "Bearer token"))
                .thenReturn(FollowResponse.of(2L, true, 5L, 3L));

        mockMvc.perform(get("/api/users/2/follow-status")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetUserId").value(2))
                .andExpect(jsonPath("$.following").value(true));

        verify(followService).getFollowStatus(2L, "Bearer token");
    }
}
