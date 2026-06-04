package com.yeginamgim.report.repository;

import com.yeginamgim.report.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    // 같은 사용자가 같은 흔적을 중복 신고했는지 확인한다.
    boolean existsByUser_UserIdAndTrace_TraceId(Long userId, Long traceId);

    void deleteByUser_UserId(Long userId);
}
