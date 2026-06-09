package com.yeginamgim.trace.dto;

import com.yeginamgim.customboard.entity.CustomBoard;
import com.yeginamgim.trace.entity.Trace;
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
    private LocalDateTime updatedAt;
    private Long likeCount;
    private Boolean liked;

    @Builder.Default
    private List<TraceElementResponse> elements = new ArrayList<>();

    public static TraceResponse from(Trace trace, List<TraceElementResponse> elements, Long likeCount) {
        return from(trace, elements, likeCount, false);
    }

    public static TraceResponse from(Trace trace, List<TraceElementResponse> elements, Long likeCount, Boolean liked) {
        Long boardId = trace.getBoard() != null
                ? trace.getBoard().getBoardId()
                : trace.getCustomBoard() != null ? trace.getCustomBoard().getCustomBoardId() : null;

        return TraceResponse.builder()
                .traceId(trace.getTraceId())
                .boardId(boardId)
                .userId(trace.getUser().getUserId())
                .nickname(trace.getUser().getNickname())
                .traceX(trace.getTraceX())
                .traceY(trace.getTraceY())
                .traceStatus(trace.getTraceStatus().name())
                .createdAt(trace.getCreatedAt())
                .updatedAt(trace.getUpdatedAt())
                .likeCount(likeCount)
                .liked(liked)
                .elements(elements)
                .build();
    }
}
