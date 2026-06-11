package com.yeginamgim.customboard.dto;

import com.yeginamgim.customboard.entity.CustomBoard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomBoardResponse {
    private Long customBoardId;
    private Long ownerId;
    private String ownerNickname;
    private String boardTitle;
    private String boardDescription;
    private String boardImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int memberCount;

    public static CustomBoardResponse from(CustomBoard board, int memberCount) {
        return CustomBoardResponse.builder()
                .customBoardId(board.getCustomBoardId())
                .ownerId(board.getUser().getUserId())
                .ownerNickname(board.getUser().getNickname())
                .boardTitle(board.getBoardTitle())
                .boardDescription(board.getBoardDescription())
                .boardImageUrl(board.getBoardImageUrl())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .memberCount(memberCount)
                .build();
    }
}
