package com.yeginamgim.customboard.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.customboard.dto.CustomBoardMemberResponse;
import com.yeginamgim.customboard.dto.InviteCreateResponse;
import com.yeginamgim.customboard.dto.InviteInfoResponse;
import com.yeginamgim.customboard.entity.CustomBoard;
import com.yeginamgim.customboard.entity.CustomBoardInvite;
import com.yeginamgim.customboard.entity.CustomBoardMember;
import com.yeginamgim.customboard.enums.BoardRole;
import com.yeginamgim.customboard.repository.CustomBoardInviteRepository;
import com.yeginamgim.customboard.repository.CustomBoardMemberRepository;
import com.yeginamgim.customboard.repository.CustomBoardRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteService {

    private static final int INVITE_EXPIRE_DAYS = 7;

    private final CustomBoardRepository customBoardRepository;
    private final CustomBoardMemberRepository customBoardMemberRepository;
    private final CustomBoardInviteRepository customBoardInviteRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final CustomBoardService customBoardService;

    @Transactional
    public InviteCreateResponse createInviteLink(Long customBoardId, String authorization) {
        UserEntity user = findUserByToken(authorization);
        CustomBoard board = findBoard(customBoardId);
        customBoardService.validateMember(customBoardId, user.getUserId());

        String inviteCode = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(INVITE_EXPIRE_DAYS);

        CustomBoardInvite invite = customBoardInviteRepository.save(
                CustomBoardInvite.create(board, user, inviteCode, expiredAt)
        );

        return InviteCreateResponse.from(invite);
    }

    @Transactional(readOnly = true)
    public InviteInfoResponse getInviteInfo(String inviteCode) {
        CustomBoardInvite invite = findInvite(inviteCode);

        if (invite.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "만료된 초대 링크입니다.");
        }

        return InviteInfoResponse.from(invite);
    }

    @Transactional
    public CustomBoardMemberResponse joinByInviteCode(String inviteCode, String authorization) {
        UserEntity user = findUserByToken(authorization);
        CustomBoardInvite invite = findInvite(inviteCode);

        if (invite.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "만료된 초대 링크입니다.");
        }

        Long customBoardId = invite.getCustomBoard().getCustomBoardId();

        if (customBoardMemberRepository.existsByCustomBoard_CustomBoardIdAndUser_UserId(customBoardId, user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 참여한 보드입니다.");
        }

        CustomBoardMember member = customBoardMemberRepository.save(
                CustomBoardMember.create(invite.getCustomBoard(), user, BoardRole.MEMBER)
        );

        return CustomBoardMemberResponse.from(member);
    }

    private CustomBoardInvite findInvite(String inviteCode) {
        return customBoardInviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않은 초대 코드입니다."));
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
