package com.yeginamgim.trace.repository;

import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.enums.TraceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceRepository extends JpaRepository<Trace, Long> {

    List<Trace> findByBoard_BoardIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(
            Long boardId,
            TraceStatus traceStatus
    );

    @Query("""
            select count(trace)
            from Trace trace
            where trace.board.kakaoPlaceId = :kakaoPlaceId
              and trace.traceStatus = com.yeginamgim.trace.enums.TraceStatus.ACTIVE
            """)
    long countActiveByKakaoPlaceId(@Param("kakaoPlaceId") String kakaoPlaceId);

    @Query("""
            select trace.board.kakaoPlaceId as kakaoPlaceId,
                   count(trace) as traceCount
            from Trace trace
            where trace.traceStatus = com.yeginamgim.trace.enums.TraceStatus.ACTIVE
            group by trace.board.kakaoPlaceId
            order by count(trace) desc, trace.board.kakaoPlaceId asc
            """)
    List<PlaceTraceCount> countActiveTracesByPlace();

    interface PlaceTraceCount {
        String getKakaoPlaceId();

        Long getTraceCount();
    }
}
