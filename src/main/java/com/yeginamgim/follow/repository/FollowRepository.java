package com.yeginamgim.follow.repository;

import com.yeginamgim.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollower_UserIdAndFollowing_UserId(Long followerId, Long followingId);

    Optional<Follow> findByFollower_UserIdAndFollowing_UserId(Long followerId, Long followingId);

    List<Follow> findByFollower_UserIdOrderByCreatedAtDesc(Long followerId);

    List<Follow> findByFollowing_UserIdOrderByCreatedAtDesc(Long followingId);

    long countByFollower_UserId(Long followerId);

    long countByFollowing_UserId(Long followingId);

    void deleteByFollower_UserIdAndFollowing_UserId(Long followerId, Long followingId);
}
