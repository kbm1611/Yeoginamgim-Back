package com.yeginamgim.trace.repository;

import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.enums.TraceStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT t
            FROM Trace t
            WHERE t.board.boardId = :boardId
              AND t.traceStatus = :traceStatus
              AND (:before IS NULL OR t.createdAt < :before)
            ORDER BY t.createdAt DESC, t.traceId DESC
            """)
    List<Trace> findBoardTracesLatest(
            @Param("boardId") Long boardId,
            @Param("traceStatus") TraceStatus traceStatus,
            @Param("before") LocalDateTime before,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM Trace t
            WHERE t.board.boardId = :boardId
              AND t.traceStatus = :traceStatus
              AND (:before IS NULL OR t.createdAt < :before)
            ORDER BY t.createdAt ASC, t.traceId ASC
            """)
    List<Trace> findBoardTracesOldest(
            @Param("boardId") Long boardId,
            @Param("traceStatus") TraceStatus traceStatus,
            @Param("before") LocalDateTime before,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM Trace t
            WHERE t.board.boardId = :boardId
              AND t.traceStatus = :traceStatus
              AND (:before IS NULL OR t.createdAt < :before)
            ORDER BY (
                SELECT COUNT(l.likeId)
                FROM TraceLike l
                WHERE l.trace = t
            ) DESC, t.createdAt DESC, t.traceId DESC
            """)
    List<Trace> findBoardTracesPopular(
            @Param("boardId") Long boardId,
            @Param("traceStatus") TraceStatus traceStatus,
            @Param("before") LocalDateTime before,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM Trace t
            WHERE t.board.boardId = :boardId
              AND t.traceStatus = :traceStatus
              AND t.traceX BETWEEN :minX AND :maxX
              AND t.traceY BETWEEN :minY AND :maxY
              AND (:before IS NULL OR t.createdAt < :before)
            ORDER BY t.createdAt DESC, t.traceId DESC
            """)
    List<Trace> findBoardAreaTracesLatest(
            @Param("boardId") Long boardId,
            @Param("traceStatus") TraceStatus traceStatus,
            @Param("minX") Integer minX,
            @Param("maxX") Integer maxX,
            @Param("minY") Integer minY,
            @Param("maxY") Integer maxY,
            @Param("before") LocalDateTime before,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM Trace t
            WHERE t.board.boardId = :boardId
              AND t.traceStatus = :traceStatus
              AND t.traceX BETWEEN :minX AND :maxX
              AND t.traceY BETWEEN :minY AND :maxY
              AND (:before IS NULL OR t.createdAt < :before)
            ORDER BY t.createdAt ASC, t.traceId ASC
            """)
    List<Trace> findBoardAreaTracesOldest(
            @Param("boardId") Long boardId,
            @Param("traceStatus") TraceStatus traceStatus,
            @Param("minX") Integer minX,
            @Param("maxX") Integer maxX,
            @Param("minY") Integer minY,
            @Param("maxY") Integer maxY,
            @Param("before") LocalDateTime before,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM Trace t
            WHERE t.board.boardId = :boardId
              AND t.traceStatus = :traceStatus
              AND t.traceX BETWEEN :minX AND :maxX
              AND t.traceY BETWEEN :minY AND :maxY
              AND (:before IS NULL OR t.createdAt < :before)
            ORDER BY (
                SELECT COUNT(l.likeId)
                FROM TraceLike l
                WHERE l.trace = t
            ) DESC, t.createdAt DESC, t.traceId DESC
            """)
    List<Trace> findBoardAreaTracesPopular(
            @Param("boardId") Long boardId,
            @Param("traceStatus") TraceStatus traceStatus,
            @Param("minX") Integer minX,
            @Param("maxX") Integer maxX,
            @Param("minY") Integer minY,
            @Param("maxY") Integer maxY,
            @Param("before") LocalDateTime before,
            Pageable pageable
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
