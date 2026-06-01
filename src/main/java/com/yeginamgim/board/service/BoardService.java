package com.yeginamgim.board.service;

import com.yeginamgim.board.dto.BoardCreateRequest;
import com.yeginamgim.board.dto.BoardDetailResponse;
import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // board_id 기준 보드 상세 조회
    public BoardDetailResponse getBoardDetail(Long boardId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "보드를 찾을 수 없습니다."));

        return toBoardDetailResponse(board);
    }

    // kakao_place_id 기준 보드 조회 또는 생성
    public BoardDetailResponse getOrCreateBoardByKakaoPlaceId(String kakaoPlaceId) {
        return getOrCreateBoard(kakaoPlaceId);
    }

    // 다른 기능에서 kakao_place_id 기준 장소 정보가 필요할 때 사용한다.
    public PlaceInfo getPlaceInfoByKakaoPlaceId(String kakaoPlaceId) {
        return findPlaceByKakaoPlaceId(kakaoPlaceId);
    }

    // kakao_place_id 기준 보드 생성 또는 기존 보드 반환
    public BoardDetailResponse createBoard(BoardCreateRequest request) {
        if (request == null || request.getKakaoPlaceId() == null || request.getKakaoPlaceId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "카카오 장소 ID는 필수입니다.");
        }

        return getOrCreateBoard(request.getKakaoPlaceId());
    }

    // kakao_place_id 기준 보드 찾기 또는 새 보드 생성
    private BoardDetailResponse getOrCreateBoard(String kakaoPlaceId) {
        if (kakaoPlaceId == null || kakaoPlaceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "카카오 장소 ID는 필수입니다.");
        }

        findPlaceByKakaoPlaceId(kakaoPlaceId);

        BoardEntity board = boardRepository.findByKakaoPlaceId(kakaoPlaceId)
                .orElseGet(() -> boardRepository.save(BoardEntity.builder()
                        .kakaoPlaceId(kakaoPlaceId)
                        .createdAt(LocalDateTime.now())
                        .build()));

        return toBoardDetailResponse(board);
    }

    // 보드 Entity를 응답 DTO로 변환
    private BoardDetailResponse toBoardDetailResponse(BoardEntity board) {
        PlaceInfo place = findPlaceByKakaoPlaceId(board.getKakaoPlaceId());

        return BoardDetailResponse.builder()
                .boardId(board.getBoardId())
                .kakaoPlaceId(board.getKakaoPlaceId())
                .createdAt(board.getCreatedAt())
                .place(place)
                .build();
    }

    // kakao_place_id 기준 places.csv 장소 검색
    private PlaceInfo findPlaceByKakaoPlaceId(String kakaoPlaceId) {
        ClassPathResource resource = new ClassPathResource("places.csv");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] columns = line.split(",", -1);
                if (columns.length < 8) {
                    continue;
                }

                if (columns[0].equals(kakaoPlaceId)) {
                    return PlaceInfo.builder()
                            .kakaoPlaceId(columns[0])
                            .placeName(columns[1])
                            .latitude(Double.parseDouble(columns[2]))
                            .longitude(Double.parseDouble(columns[3]))
                            .phone(columns[4])
                            .address(columns[5])
                            .kakaoMapUrl(columns[6])
                            .groupName(columns[7])
                            .build();
                }
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "장소 CSV 파일을 읽을 수 없습니다.");
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CSV에서 카카오 장소 정보를 찾을 수 없습니다.");
    }
}
