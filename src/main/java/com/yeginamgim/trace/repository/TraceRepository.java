package com.yeginamgim.trace.repository;

import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.enums.TraceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceRepository extends JpaRepository<Trace, Long> {

    List<Trace> findByBoard_BoardIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(
            Long boardId,
            TraceStatus traceStatus
    );
}
