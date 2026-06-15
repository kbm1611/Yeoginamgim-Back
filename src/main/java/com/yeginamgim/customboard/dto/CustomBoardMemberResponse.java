package com.yeginamgim.customboard.dto;

import com.yeginamgim.customboard.entity.CustomBoardMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomBoardMemberResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private Instant joinedAt;

    public static CustomBoardMemberResponse from(CustomBoardMember member) {
        return CustomBoardMemberResponse.builder()
                .userId(member.getUser().getUserId())
                .nickname(member.getUser().getNickname())
                .profileImageUrl(member.getUser().getProfileImageUrl())
                .role(member.getRole().name())
                .joinedAt(member.getCreatedAt())
                .build();
    }
}
