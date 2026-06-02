package com.yeginamgim.board.service;

import com.yeginamgim.board.dto.BoardCreateRequest;
import com.yeginamgim.board.dto.BoardDetailResponse;
import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.place.repository.PlaceCsvStore;
import com.yeginamgim.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final PlaceService placeService;
    private final PlaceCsvStore placeCsvStore;

    public BoardDetailResponse getBoardDetail(Long boardId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found."));

        return toBoardDetailResponse(board);
    }

    public BoardDetailResponse getOrCreateBoardByKakaoPlaceId(String kakaoPlaceId) {
        return getOrCreateBoard(kakaoPlaceId);
    }

    public PlaceInfo getPlaceInfoByKakaoPlaceId(String kakaoPlaceId) {
        return findPlaceByKakaoPlaceId(kakaoPlaceId);
    }

    public BoardDetailResponse createBoard(BoardCreateRequest request) {
        validateCreateRequest(request);
        savePlaceSnapshotIfNeeded(request);

        return getOrCreateBoard(request.getKakaoPlaceId());
    }

    private BoardDetailResponse getOrCreateBoard(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
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

    private void validateCreateRequest(BoardCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getKakaoPlaceId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kakaoPlaceId is required.");
        }
    }

    // 장소 정보가 CSV 캐시에 없다면 CSV에 저장하는 함수.
    private void savePlaceSnapshotIfNeeded(BoardCreateRequest request) {
        if (placeCsvStore.findByKakaoPlaceId(request.getKakaoPlaceId()).isPresent()) {
            return;
        }
        if (!hasPlaceSnapshot(request)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place snapshot is required for uncached place.");
        }

        placeCsvStore.saveIfAbsent(PlaceInfo.builder()
                .kakaoPlaceId(request.getKakaoPlaceId())
                .placeName(request.getPlaceName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .address(request.getAddress())
                .kakaoMapUrl(request.getKakaoMapUrl())
                .groupName(request.getGroupName())
                .build());
    }

    // 요청에 CSV로 저장할 수 있을 만큼의 장보 정보가 있는지 확인. 장소명, 위도, 경도, 카테고리 중 하나라도 없으면 false
    private boolean hasPlaceSnapshot(BoardCreateRequest request) {
        return StringUtils.hasText(request.getPlaceName())
                && request.getLatitude() != null
                && request.getLongitude() != null
                && StringUtils.hasText(request.getGroupName());
    }
}
