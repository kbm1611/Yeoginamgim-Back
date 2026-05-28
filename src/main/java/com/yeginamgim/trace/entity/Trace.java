package com.yeginamgim.trace.entity;

import com.yeginamgim.global.entity.BaseTime;
import com.yeginamgim.trace.enums.TraceStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trace")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trace extends BaseTime {

    /** 흔적 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trace_id")
    private Long traceId;

    /** 흔적을 남긴 유저 (FK → users.user_id) - 지연 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
     */

    /** 흔적이 속한 보드 (FK → boards.board_id) - 지연 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;
     */

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
    private TraceStatus traceStatus = TraceStatus.ACTIVE;

    /** 흔적 생성 빌더
    @Builder
    public Trace(User user, Board board, Integer traceX, Integer traceY) {
        this.user = user;
        this.board = board;
        this.traceX = traceX;
        this.traceY = traceY;
        this.traceStatus = TraceStatus.ACTIVE;
    }
     */

    /** 흔적 숨기기 (ACTIVE → HIDE) */
    public void hide() {
        this.traceStatus = TraceStatus.HIDE;
    }
}
