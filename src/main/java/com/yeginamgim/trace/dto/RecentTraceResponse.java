package com.yeginamgim.trace.dto;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.trace.entity.Trace;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RecentTraceResponse {
    private Long traceId;
    private Long boardId;
    private String placeName;
    private String previewText;
    private String imageUrl;
    private Instant createdAt;
    private Long likeCount;
    private String nickname;

    public static RecentTraceResponse from(
            Trace trace,
            PlaceInfo placeInfo,
            String previewText,
            String imageUrl,
            Long likeCount
    ) {
        return RecentTraceResponse.builder()
                .traceId(trace.getTraceId())
                .boardId(trace.getBoard().getBoardId())
                .placeName(placeInfo == null ? "장소 보드" : placeInfo.getPlaceName())
                .previewText(previewText)
                .imageUrl(imageUrl)
                .createdAt(trace.getCreatedAt())
                .likeCount(likeCount)
                .nickname(trace.getUser().getNickname())
                .build();
    }
}
