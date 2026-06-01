package com.yeginamgim.archive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 날짜별 기록 조회 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveCalendarResponse {
    private Long userId;
    private Integer year;
    private Integer month;

    @Builder.Default
    private List<ArchiveDateGroupResponse> days = new ArrayList<>();
}
