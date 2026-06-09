package com.yeginamgim.customboard.repository;

import com.yeginamgim.customboard.entity.CustomBoardInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomBoardInviteRepository extends JpaRepository<CustomBoardInvite, Long> {

    Optional<CustomBoardInvite> findByInviteCode(String inviteCode);

    void deleteByCustomBoard_CustomBoardId(Long customBoardId);
}
