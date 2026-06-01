package com.yeginamgim.archive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 공간별 추억 아카이브 목록 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveBoardListResponse {
    private Long userId;

    @Builder.Default
    private List<ArchiveBoardGroupResponse> boards = new ArrayList<>();
}
