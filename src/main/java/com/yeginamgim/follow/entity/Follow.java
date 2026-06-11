package com.yeginamgim.follow.entity;

import com.yeginamgim.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_follow",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}),
        indexes = {
                @Index(name = "idx_user_follow_follower_id", columnList = "follower_id"),
                @Index(name = "idx_user_follow_following_id", columnList = "following_id")
        }
)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    private Long followId;

    // User who follows another user.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private UserEntity follower;

    // User being followed.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private UserEntity following;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static Follow create(UserEntity follower, UserEntity following) {
        return Follow.builder()
                .follower(follower)
                .following(following)
                .build();
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
