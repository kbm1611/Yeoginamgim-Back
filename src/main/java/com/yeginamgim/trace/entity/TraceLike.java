package com.yeginamgim.trace.entity;


import com.yeginamgim.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 좋아요 테이블 Entity
 * 동일 유저가 같은 흔적에 중복 좋아요 불가 (UNIQUE 제약)
 *
 * like_id | user_id | trace_id | created_at
 */
@Entity
@Table(name = "`like`",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "trace_id"}))
@Data
@NoArgsConstructor
public class TraceLike {
    /** 좋아요 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    /** 좋아요 누른 유저 (FK -> users.user_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** 좋아요 눌린 흔적 (FK -> trace.trace_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trace_id", nullable = false)
    private Trace trace;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 좋아요 생성 빌더 */
    @Builder
    public TraceLike(UserEntity user, Trace trace) {
        this.user = user;
        this.trace = trace;
    }

    public static TraceLike create(UserEntity user, Trace trace) {
        return TraceLike.builder()
                .user(user)
                .trace(trace)
                .build();
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
