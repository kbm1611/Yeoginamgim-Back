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
    private String inviteUrl;
    private Instant expiredAt;

    public static InviteCreateResponse from(CustomBoardInvite invite) {
        return from(invite, null);
    }

    public static InviteCreateResponse from(CustomBoardInvite invite, String frontendBaseUrl) {
        return InviteCreateResponse.builder()
                .inviteCode(invite.getInviteCode())
                .inviteUrl(buildInviteUrl(frontendBaseUrl, invite.getInviteCode()))
                .expiredAt(invite.getExpiredAt())
                .build();
    }

    private static String buildInviteUrl(String frontendBaseUrl, String inviteCode) {
        if (frontendBaseUrl == null || frontendBaseUrl.isBlank()) {
            return null;
        }

        String normalizedBaseUrl = frontendBaseUrl.trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }

        return normalizedBaseUrl + "/board/join/" + inviteCode;
    }
}
