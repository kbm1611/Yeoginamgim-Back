package com.yeginamgim.customboard.dto;

import com.yeginamgim.customboard.entity.CustomBoard;
import com.yeginamgim.customboard.entity.CustomBoardInvite;
import com.yeginamgim.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InviteInfoResponseTest {

    @Test
    void fromIncludesBoardOwnerNicknameSeparatelyFromInviteCreator() {
        UserEntity owner = UserEntity.builder()
                .userId(1L)
                .nickname("보드장")
                .build();
        UserEntity inviter = UserEntity.builder()
                .userId(2L)
                .nickname("초대한사람")
                .build();
        CustomBoard board = CustomBoard.builder()
                .customBoardId(33L)
                .user(owner)
                .boardTitle("여행 보드")
                .build();
        CustomBoardInvite invite = CustomBoardInvite.builder()
                .customBoard(board)
                .user(inviter)
                .inviteCode("abc123")
                .expiredAt(Instant.parse("2026-06-15T00:00:00Z"))
                .build();

        InviteInfoResponse response = InviteInfoResponse.from(invite);

        assertThat(response.getOwnerNickname()).isEqualTo("보드장");
        assertThat(response.getInviterNickname()).isEqualTo("초대한사람");
    }
}
