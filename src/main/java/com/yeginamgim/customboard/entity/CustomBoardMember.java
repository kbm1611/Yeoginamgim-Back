package com.yeginamgim.customboard.entity;

import com.yeginamgim.customboard.enums.BoardRole;
import com.yeginamgim.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "custom_board_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"custom_board_id", "user_id"})
)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CustomBoardMember {

    /** 멤버 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    /** 소속 커스텀 보드 (FK -> custom_board.custom_board_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_board_id", nullable = false)
    private CustomBoard customBoard;

    /** 멤버 유저 (FK -> users.user_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /**
     * 멤버 역할
     * OWNER  : 보드 생성자
     * MEMBER : 초대된 멤버
     */
    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BoardRole role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

    public static CustomBoardMember create(CustomBoard customBoard, UserEntity user, BoardRole role) {
        return CustomBoardMember.builder()
                .customBoard(customBoard)
                .user(user)
                .role(role)
                .build();
    }
}
