package com.yeginamgim.trace.entity;

import com.yeginamgim.global.entity.BaseTime;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.customboard.entity.CustomBoard;
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

    /** 흔적이 속한 장소 보드 (FK -> board.board_id) — 커스텀 보드 흔적이면 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = true)
    private BoardEntity board;

    /** 흔적이 속한 커스텀 보드 (FK -> custom_board.custom_board_id) — 장소 보드 흔적이면 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_board_id", nullable = true)
    private CustomBoard customBoard;

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

    /** board와 customBoard 중 하나만 설정되었는지 검증 */
    public boolean isValidBoardAssignment() {
        return (this.board != null) ^ (this.customBoard != null);
    }

    /** 장소 보드 흔적 생성 */
    public static Trace create(BoardEntity board, UserEntity user, Integer traceX, Integer traceY) {
        return Trace.builder()
                .board(board)
                .user(user)
                .traceX(traceX)
                .traceY(traceY)
                .traceStatus(TraceStatus.ACTIVE)
                .build();
    }

    /** 커스텀 보드 흔적 생성 */
    public static Trace createForCustomBoard(CustomBoard customBoard, UserEntity user, Integer traceX, Integer traceY) {
        return Trace.builder()
                .customBoard(customBoard)
                .user(user)
                .traceX(traceX)
                .traceY(traceY)
                .traceStatus(TraceStatus.ACTIVE)
                .build();
    }
}
