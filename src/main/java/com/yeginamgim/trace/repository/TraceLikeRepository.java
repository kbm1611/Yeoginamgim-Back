package com.yeginamgim.trace.repository;

import com.yeginamgim.trace.entity.TraceLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceLikeRepository extends JpaRepository<TraceLike, Long> {

    boolean existsByUser_UserIdAndTrace_TraceId(Long userId, Long traceId);

    long countByTrace_TraceId(Long traceId);

    void deleteByUser_UserIdAndTrace_TraceId(Long userId, Long traceId);

    void deleteByUser_UserId(Long userId);
}
