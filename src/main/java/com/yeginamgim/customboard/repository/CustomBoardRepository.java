package com.yeginamgim.customboard.repository;

import com.yeginamgim.customboard.entity.CustomBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomBoardRepository extends JpaRepository<CustomBoard, Long> {

    @Query("""
            SELECT cb
            FROM CustomBoard cb
            JOIN CustomBoardMember m ON m.customBoard = cb
            WHERE m.user.userId = :userId
            ORDER BY cb.createdAt DESC, cb.customBoardId DESC
            """)
    List<CustomBoard> findAllByMemberUserId(@Param("userId") Long userId);
}
