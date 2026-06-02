package com.yeginamgim.board.repository;

import com.yeginamgim.board.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    // kakao_place_id 중복 확인이나 보드 조회가 필요할 때 사용한다.
    Optional<BoardEntity> findByKakaoPlaceId(String kakaoPlaceId);

    // 여러 장소의 보드 정보를 한 번에 조회하기 위한 N+1 개선용 메서드
    List<BoardEntity> findByKakaoPlaceIdIn(Collection<String> kakaoPlaceIds);
}
