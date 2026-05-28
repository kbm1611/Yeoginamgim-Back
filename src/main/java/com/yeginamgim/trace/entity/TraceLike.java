package com.yeginamgim.trace.entity;


import com.yeginamgim.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;

/**
 * 좋아요 테이블 Entity
 * 동일 유저가 같은 흔적에 중복 좋아요 불가 (UNIQUE 제약)
 *
 * like_id | user_id | trace_id | created_at
 */
@Entity
@Table(name = "trace_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "trace_id"}))
@Data
@NoArgsConstructor
public class TraceLike extends BaseTime {
    /** 좋아요 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    /** 좋아요 누른 유저 (FK → users.user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
     */

    /** 좋아요 눌린 흔적 (FK → traces.trace_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trace_id", nullable = false)
    private Trace trace;

    /** 좋아요 생성 빌더
    @Builder
    public TraceLike(User user, Trace trace) {
        this.user = user;
        this.trace = trace;
    }
     */
}
