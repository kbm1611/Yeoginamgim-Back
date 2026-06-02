package com.yeginamgim.trace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 추천/좋아요 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceLikeResponse {
    private Long traceId;
    private Boolean liked;
    private Long likeCount;

    public static TraceLikeResponse of(Long traceId, boolean liked, Long likeCount) {
        return TraceLikeResponse.builder()
                .traceId(traceId)
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }
}
