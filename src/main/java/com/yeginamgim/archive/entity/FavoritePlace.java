package com.yeginamgim.archive.entity;

import com.yeginamgim.global.entity.BaseTime;
import com.yeginamgim.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_place",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "kakao_place_id"}))
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FavoritePlace extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_place_id")
    private Long favoritePlaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "kakao_place_id", nullable = false, length = 100)
    private String kakaoPlaceId;

    public static FavoritePlace create(UserEntity user, String kakaoPlaceId) {
        return FavoritePlace.builder()
                .user(user)
                .kakaoPlaceId(kakaoPlaceId)
                .build();
    }
}
