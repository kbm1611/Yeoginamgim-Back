package com.yeginamgim.customboard.entity;

import com.yeginamgim.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "custom_board_invite")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CustomBoardInvite {

    /** 초대 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long inviteId;

    /** 초대 대상 커스텀 보드 (FK -> custom_board.custom_board_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_board_id", nullable = false)
    private CustomBoard customBoard;

    /** 초대 코드 (UNIQUE) */
    @Column(name = "invite_code", nullable = false, unique = true, length = 100)
    private String inviteCode;

    /** 초대 링크 생성자 (FK -> users.user_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** 초대 링크 만료 시각 */
    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiredAt);
    }

    public static CustomBoardInvite create(CustomBoard customBoard, UserEntity user, String inviteCode, Instant expiredAt) {
        return CustomBoardInvite.builder()
                .customBoard(customBoard)
                .user(user)
                .inviteCode(inviteCode)
                .expiredAt(expiredAt)
                .build();
    }
}
