package com.yeginamgim.archive.dto;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.trace.dto.TraceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 한 공간에 남긴 흔적들을 묶은 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveBoardGroupResponse {
    private Long boardId;
    private String kakaoPlaceId;
    private String placeName;
    private String groupName;
    private Integer traceCount;

    @Builder.Default
    private List<TraceResponse> traces = new ArrayList<>();

    public static ArchiveBoardGroupResponse from(
            BoardEntity board,
            PlaceInfo place,
            List<TraceResponse> traces
    ) {
        return ArchiveBoardGroupResponse.builder()
                .boardId(board.getBoardId())
                .kakaoPlaceId(board.getKakaoPlaceId())
                .placeName(place.getPlaceName())
                .groupName(place.getGroupName())
                .traceCount(traces.size())
                .traces(traces)
                .build();
    }
}
