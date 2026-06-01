package com.yeginamgim.trace.repository;

import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.enums.TraceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TraceRepository extends JpaRepository<Trace, Long> {

    List<Trace> findByBoard_BoardIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(
            Long boardId,
            TraceStatus traceStatus
    );

    List<Trace> findByUser_UserIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(
            Long userId,
            TraceStatus traceStatus
    );

    Optional<Trace> findByTraceIdAndUser_UserIdAndTraceStatus(
            Long traceId,
            Long userId,
            TraceStatus traceStatus
    );

    List<Trace> findByUser_UserIdAndTraceStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescTraceIdDesc(
            Long userId,
            TraceStatus traceStatus,
            LocalDateTime startAt,
            LocalDateTime endAt
    );
}
