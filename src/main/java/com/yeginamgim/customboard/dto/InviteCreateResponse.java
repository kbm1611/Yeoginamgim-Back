package com.yeginamgim.customboard.dto;

import com.yeginamgim.customboard.entity.CustomBoardInvite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteCreateResponse {
    private String inviteCode;
    private Instant expiredAt;

    public static InviteCreateResponse from(CustomBoardInvite invite) {
        return InviteCreateResponse.builder()
                .inviteCode(invite.getInviteCode())
                .expiredAt(invite.getExpiredAt())
                .build();
    }
}
