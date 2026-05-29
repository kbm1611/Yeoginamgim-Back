package com.yeginamgim.trace.repository;

import com.yeginamgim.trace.entity.TraceElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TraceElementRepository extends JpaRepository<TraceElement, Long> {

    List<TraceElement> findByTrace_TraceIdOrderByElementIdAsc(Long traceId);

    List<TraceElement> findByTrace_TraceIdInOrderByElementIdAsc(Collection<Long> traceIds);
}
