package com.yeginamgim.trace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 흔적 조회 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceResponse {
    private Long traceId;
    private Long boardId;
    private Long userId;
    private String nickname;
    private Integer traceX;
    private Integer traceY;
    private String traceStatus;
    private LocalDateTime createdAt;
    private Long likeCount;

    @Builder.Default
    private List<TraceElementResponse> elements = new ArrayList<>();
}
