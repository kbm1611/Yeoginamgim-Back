package com.yeginamgim.customboard.dto;

import com.yeginamgim.customboard.entity.CustomBoard;
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
public class InviteInfoResponse {
    private Long customBoardId;
    private String boardTitle;
    private String boardImageUrl;
    private String inviterNickname;
    private Instant expiredAt;

    public static InviteInfoResponse from(CustomBoardInvite invite) {
        CustomBoard board = invite.getCustomBoard();
        return InviteInfoResponse.builder()
                .customBoardId(board.getCustomBoardId())
                .boardTitle(board.getBoardTitle())
                .boardImageUrl(board.getBoardImageUrl())
                .inviterNickname(invite.getUser().getNickname())
                .expiredAt(invite.getExpiredAt())
                .build();
    }
}
