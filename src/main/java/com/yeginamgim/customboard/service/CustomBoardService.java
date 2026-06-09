package com.yeginamgim.customboard.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.customboard.dto.CustomBoardCreateRequest;
import com.yeginamgim.customboard.dto.CustomBoardMemberResponse;
import com.yeginamgim.customboard.dto.CustomBoardResponse;
import com.yeginamgim.customboard.dto.CustomBoardUpdateRequest;
import com.yeginamgim.customboard.entity.CustomBoard;
import com.yeginamgim.customboard.entity.CustomBoardMember;
import com.yeginamgim.customboard.enums.BoardRole;
import com.yeginamgim.customboard.repository.CustomBoardInviteRepository;
import com.yeginamgim.customboard.repository.CustomBoardMemberRepository;
import com.yeginamgim.customboard.repository.CustomBoardRepository;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomBoardService {

    private final CustomBoardRepository customBoardRepository;
    private final CustomBoardMemberRepository customBoardMemberRepository;
    private final CustomBoardInviteRepository customBoardInviteRepository;
    private final TraceRepository traceRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    @Transactional
    public CustomBoardResponse createBoard(String authorization, CustomBoardCreateRequest request) {
        if (request == null || request.getBoardTitle() == null || request.getBoardTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "보드 제목은 필수입니다.");
        }

        UserEntity user = findUserByToken(authorization);
        CustomBoard board = customBoardRepository.save(
                CustomBoard.create(user, request.getBoardTitle(), request.getBoardDescription(), request.getBoardImageUrl())
        );
        customBoardMemberRepository.save(CustomBoardMember.create(board, user, BoardRole.OWNER));

        return CustomBoardResponse.from(board, 1);
    }

    @Transactional(readOnly = true)
    public CustomBoardResponse getBoardDetail(Long customBoardId, String authorization) {
        UserEntity user = findUserByToken(authorization);
        CustomBoard board = findBoard(customBoardId);
        validateMember(customBoardId, user.getUserId());

        int memberCount = customBoardMemberRepository.findByCustomBoard_CustomBoardIdOrderByCreatedAtAsc(customBoardId).size();
        return CustomBoardResponse.from(board, memberCount);
    }

    @Transactional(readOnly = true)
    public List<CustomBoardResponse> getMyBoards(String authorization) {
        UserEntity user = findUserByToken(authorization);
        List<CustomBoard> boards = customBoardRepository.findAllByMemberUserId(user.getUserId());

        return boards.stream()
                .map(board -> {
                    int memberCount = customBoardMemberRepository
                            .findByCustomBoard_CustomBoardIdOrderByCreatedAtAsc(board.getCustomBoardId()).size();
                    return CustomBoardResponse.from(board, memberCount);
                })
                .toList();
    }

    @Transactional
    public CustomBoardResponse updateBoard(Long customBoardId, String authorization, CustomBoardUpdateRequest request) {
        UserEntity user = findUserByToken(authorization);
        CustomBoard board = findBoard(customBoardId);
        validateOwner(customBoardId, user.getUserId());

        if (request.getBoardTitle() != null && !request.getBoardTitle().isBlank()) {
            board.setBoardTitle(request.getBoardTitle());
        }
        if (request.getBoardDescription() != null) {
            board.setBoardDescription(request.getBoardDescription());
        }
        if (request.getBoardImageUrl() != null) {
            board.setBoardImageUrl(request.getBoardImageUrl());
        }

        int memberCount = customBoardMemberRepository.findByCustomBoard_CustomBoardIdOrderByCreatedAtAsc(customBoardId).size();
        return CustomBoardResponse.from(board, memberCount);
    }

    @Transactional
    public void deleteBoard(Long customBoardId, String authorization) {
        UserEntity user = findUserByToken(authorization);
        findBoard(customBoardId);
        validateOwner(customBoardId, user.getUserId());

        customBoardInviteRepository.deleteByCustomBoard_CustomBoardId(customBoardId);
        customBoardMemberRepository.deleteByCustomBoard_CustomBoardId(customBoardId);
        customBoardRepository.deleteById(customBoardId);
    }

    @Transactional(readOnly = true)
    public List<CustomBoardMemberResponse> getMembers(Long customBoardId, String authorization) {
        UserEntity user = findUserByToken(authorization);
        findBoard(customBoardId);
        validateMember(customBoardId, user.getUserId());

        return customBoardMemberRepository
                .findByCustomBoard_CustomBoardIdOrderByCreatedAtAsc(customBoardId)
                .stream()
                .map(CustomBoardMemberResponse::from)
                .toList();
    }

    @Transactional
    public void kickMember(Long customBoardId, Long targetUserId, String authorization) {
        UserEntity user = findUserByToken(authorization);
        validateOwner(customBoardId, user.getUserId());

        if (user.getUserId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인을 강퇴할 수 없습니다.");
        }

        CustomBoardMember target = customBoardMemberRepository
                .findByCustomBoard_CustomBoardIdAndUser_UserId(customBoardId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 멤버를 찾을 수 없습니다."));

        customBoardMemberRepository.delete(target);
    }

    @Transactional
    public void leaveBoard(Long customBoardId, String authorization) {
        UserEntity user = findUserByToken(authorization);
        validateMember(customBoardId, user.getUserId());

        if (customBoardMemberRepository.existsByCustomBoard_CustomBoardIdAndUser_UserIdAndRole(
                customBoardId, user.getUserId(), BoardRole.OWNER)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OWNER는 보드를 탈퇴할 수 없습니다. 보드를 삭제해 주세요.");
        }

        CustomBoardMember member = customBoardMemberRepository
                .findByCustomBoard_CustomBoardIdAndUser_UserId(customBoardId, user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버 정보를 찾을 수 없습니다."));

        customBoardMemberRepository.delete(member);
    }

    void validateMember(Long customBoardId, Long userId) {
        if (!customBoardMemberRepository.existsByCustomBoard_CustomBoardIdAndUser_UserId(customBoardId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "보드 멤버만 접근할 수 있습니다.");
        }
    }

    void validateOwner(Long customBoardId, Long userId) {
        if (!customBoardMemberRepository.existsByCustomBoard_CustomBoardIdAndUser_UserIdAndRole(
                customBoardId, userId, BoardRole.OWNER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "보드 OWNER만 접근할 수 있습니다.");
        }
    }

    private CustomBoard findBoard(Long customBoardId) {
        return customBoardRepository.findById(customBoardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "커스텀 보드를 찾을 수 없습니다."));
    }

    private UserEntity findUserByToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        String email = jwtService.getClaim(authorization);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
