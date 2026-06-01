package com.yeginamgim.trace.entity;

import com.yeginamgim.global.entity.BaseTime;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.trace.enums.TraceStatus;
import com.yeginamgim.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trace")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Trace extends BaseTime {

    /** 흔적 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trace_id")
    private Long traceId;

    /** 흔적을 남긴 유저 (FK -> users.user_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** 흔적이 속한 보드 (FK -> board.board_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    /** 보드 내 X 좌표 (INT, 픽셀 기준) */
    @Column(name = "trace_x", nullable = false)
    private Integer traceX;

    /** 보드 내 Y 좌표 (INT, 픽셀 기준) */
    @Column(name = "trace_y", nullable = false)
    private Integer traceY;

    /**
     * 흔적 상태 (VARCHAR(20), DEFAULT 'ACTIVE')
     * ACTIVE : 정상 노출
     * HIDE   : 유저가 직접 숨긴 상태
     */
    @Column(name = "trace_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TraceStatus traceStatus = TraceStatus.ACTIVE;

    /** 흔적 숨기기 (ACTIVE → HIDE) */
    public void hide() {
        this.traceStatus = TraceStatus.HIDE;
    }
}
