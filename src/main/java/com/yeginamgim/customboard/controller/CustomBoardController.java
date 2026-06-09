package com.yeginamgim.customboard.controller;

import com.yeginamgim.customboard.dto.CustomBoardCreateRequest;
import com.yeginamgim.customboard.dto.CustomBoardMemberResponse;
import com.yeginamgim.customboard.dto.CustomBoardResponse;
import com.yeginamgim.customboard.dto.CustomBoardUpdateRequest;
import com.yeginamgim.customboard.dto.InviteCreateResponse;
import com.yeginamgim.customboard.dto.InviteInfoResponse;
import com.yeginamgim.customboard.service.CustomBoardService;
import com.yeginamgim.customboard.service.InviteService;
import com.yeginamgim.trace.dto.TraceCreateRequest;
import com.yeginamgim.trace.dto.TraceResponse;
import com.yeginamgim.trace.service.TraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CustomBoardController {

    private final CustomBoardService customBoardService;
    private final InviteService inviteService;
    private final TraceService traceService;

    // 커스텀 보드 생성
    @PostMapping("/custom-boards")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomBoardResponse createBoard(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CustomBoardCreateRequest request
    ) {
        return customBoardService.createBoard(authorization, request);
    }

    // 내 보드 목록 조회
    @GetMapping("/custom-boards/my")
    public List<CustomBoardResponse> getMyBoards(
            @RequestHeader("Authorization") String authorization
    ) {
        return customBoardService.getMyBoards(authorization);
    }

    // 보드 상세 조회
    @GetMapping("/custom-boards/{customBoardId}")
    public CustomBoardResponse getBoardDetail(
            @PathVariable Long customBoardId,
            @RequestHeader("Authorization") String authorization
    ) {
        return customBoardService.getBoardDetail(customBoardId, authorization);
    }

    // 보드 수정 (OWNER)
    @PatchMapping("/custom-boards/{customBoardId}")
    public CustomBoardResponse updateBoard(
            @PathVariable Long customBoardId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody CustomBoardUpdateRequest request
    ) {
        return customBoardService.updateBoard(customBoardId, authorization, request);
    }

    // 보드 삭제 (OWNER)
    @DeleteMapping("/custom-boards/{customBoardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBoard(
            @PathVariable Long customBoardId,
            @RequestHeader("Authorization") String authorization
    ) {
        customBoardService.deleteBoard(customBoardId, authorization);
    }

    // 멤버 목록 조회
    @GetMapping("/custom-boards/{customBoardId}/members")
    public List<CustomBoardMemberResponse> getMembers(
            @PathVariable Long customBoardId,
            @RequestHeader("Authorization") String authorization
    ) {
        return customBoardService.getMembers(customBoardId, authorization);
    }

    // 멤버 강퇴 (OWNER)
    @DeleteMapping("/custom-boards/{customBoardId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickMember(
            @PathVariable Long customBoardId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorization
    ) {
        customBoardService.kickMember(customBoardId, userId, authorization);
    }

    // 보드 탈퇴 (본인)
    @DeleteMapping("/custom-boards/{customBoardId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveBoard(
            @PathVariable Long customBoardId,
            @RequestHeader("Authorization") String authorization
    ) {
        customBoardService.leaveBoard(customBoardId, authorization);
    }

    // 초대 링크 생성
    @PostMapping("/custom-boards/{customBoardId}/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteCreateResponse createInviteLink(
            @PathVariable Long customBoardId,
            @RequestHeader("Authorization") String authorization
    ) {
        return inviteService.createInviteLink(customBoardId, authorization);
    }

    // 초대 코드로 보드 정보 미리 조회
    @GetMapping("/custom-boards/join/{inviteCode}")
    public InviteInfoResponse getInviteInfo(
            @PathVariable String inviteCode
    ) {
        return inviteService.getInviteInfo(inviteCode);
    }

    // 초대 코드로 보드 참여
    @PostMapping("/custom-boards/join/{inviteCode}")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomBoardMemberResponse joinByInviteCode(
            @PathVariable String inviteCode,
            @RequestHeader("Authorization") String authorization
    ) {
        return inviteService.joinByInviteCode(inviteCode, authorization);
    }

    // 커스텀 보드 흔적 생성
    @PostMapping("/custom-boards/{customBoardId}/traces")
    @ResponseStatus(HttpStatus.CREATED)
    public TraceResponse createTrace(
            @PathVariable Long customBoardId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody TraceCreateRequest request
    ) {
        return traceService.createTraceForCustomBoard(customBoardId, authorization, request);
    }

    // 커스텀 보드 흔적 목록 조회
    @GetMapping("/custom-boards/{customBoardId}/traces")
    public List<TraceResponse> getTraces(
            @PathVariable Long customBoardId,
            @RequestHeader("Authorization") String authorization
    ) {
        return traceService.getTracesByCustomBoardId(customBoardId, authorization);
    }

    // 커스텀 보드 흔적 삭제 (작성자 또는 OWNER)
    @DeleteMapping("/custom-boards/traces/{traceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideTrace(
            @PathVariable Long traceId,
            @RequestHeader("Authorization") String authorization
    ) {
        traceService.hideCustomBoardTrace(traceId, authorization);
    }
}
