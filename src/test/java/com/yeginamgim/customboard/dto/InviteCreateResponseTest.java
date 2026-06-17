package com.yeginamgim.customboard.dto;

import com.yeginamgim.customboard.entity.CustomBoardInvite;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InviteCreateResponseTest {

    @Test
    void fromBuildsInviteUrlFromConfiguredFrontendBaseUrl() {
        CustomBoardInvite invite = CustomBoardInvite.builder()
                .inviteCode("abc123")
                .expiredAt(Instant.parse("2026-06-15T00:00:00Z"))
                .build();

        InviteCreateResponse response = InviteCreateResponse.from(
                invite,
                "http://192.168.219.150:5173/"
        );

        assertThat(response.getInviteCode()).isEqualTo("abc123");
        assertThat(response.getInviteUrl()).isEqualTo("http://192.168.219.150:5173/board/join/abc123");
    }
}
