package com.yeginamgim.archive.repository;

import com.yeginamgim.archive.entity.FavoritePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    List<FavoritePlace> findByUser_UserIdOrderByCreatedAtDescFavoritePlaceIdDesc(Long userId);

    Optional<FavoritePlace> findByUser_UserIdAndKakaoPlaceId(Long userId, String kakaoPlaceId);

    boolean existsByUser_UserIdAndKakaoPlaceId(Long userId, String kakaoPlaceId);

    void deleteByUser_UserIdAndKakaoPlaceId(Long userId, String kakaoPlaceId);
}
