package com.yeginamgim.customboard.repository;

import com.yeginamgim.customboard.entity.CustomBoardMember;
import com.yeginamgim.customboard.enums.BoardRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomBoardMemberRepository extends JpaRepository<CustomBoardMember, Long> {

    boolean existsByCustomBoard_CustomBoardIdAndUser_UserId(Long customBoardId, Long userId);

    Optional<CustomBoardMember> findByCustomBoard_CustomBoardIdAndUser_UserId(Long customBoardId, Long userId);

    List<CustomBoardMember> findByCustomBoard_CustomBoardIdOrderByCreatedAtAsc(Long customBoardId);

    void deleteByCustomBoard_CustomBoardId(Long customBoardId);

    boolean existsByCustomBoard_CustomBoardIdAndUser_UserIdAndRole(Long customBoardId, Long userId, BoardRole role);
}
