package com.yeginamgim.report.repository;

import com.yeginamgim.report.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    // 같은 사용자가 같은 흔적을 중복 신고했는지 확인한다.
    boolean existsByUser_UserIdAndTrace_TraceId(Long userId, Long traceId);

    long countByTrace_TraceId(Long traceId);

    List<ReportEntity> findByTrace_TraceId(Long traceId);

    void deleteByUser_UserId(Long userId);
}
