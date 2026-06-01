package com.yeginamgim.board.service;

import com.yeginamgim.board.dto.BoardCreateRequest;
import com.yeginamgim.board.dto.BoardDetailResponse;
import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final PlaceService placeService;

    public BoardDetailResponse getBoardDetail(Long boardId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found."));

        return toBoardDetailResponse(board);
    }

    public BoardDetailResponse getOrCreateBoardByKakaoPlaceId(String kakaoPlaceId) {
        return getOrCreateBoard(kakaoPlaceId);
    }

    public BoardDetailResponse createBoard(BoardCreateRequest request) {
        if (request == null || request.getKakaoPlaceId() == null || request.getKakaoPlaceId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kakaoPlaceId is required.");
        }

        return getOrCreateBoard(request.getKakaoPlaceId());
    }

    private BoardDetailResponse getOrCreateBoard(String kakaoPlaceId) {
        if (kakaoPlaceId == null || kakaoPlaceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kakaoPlaceId is required.");
        }

        findPlaceByKakaoPlaceId(kakaoPlaceId);

        BoardEntity board = boardRepository.findByKakaoPlaceId(kakaoPlaceId)
                .orElseGet(() -> boardRepository.save(BoardEntity.builder()
                        .kakaoPlaceId(kakaoPlaceId)
                        .createdAt(LocalDateTime.now())
                        .build()));

        return toBoardDetailResponse(board);
    }

    private BoardDetailResponse toBoardDetailResponse(BoardEntity board) {
        PlaceInfo place = findPlaceByKakaoPlaceId(board.getKakaoPlaceId());

        return BoardDetailResponse.builder()
                .boardId(board.getBoardId())
                .kakaoPlaceId(board.getKakaoPlaceId())
                .createdAt(board.getCreatedAt())
                .place(place)
                .build();
    }

    private PlaceInfo findPlaceByKakaoPlaceId(String kakaoPlaceId) {
        return placeService.findPlaceInfoByKakaoPlaceId(kakaoPlaceId);
    }
}
