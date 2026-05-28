package com.yeginamgim.board.service;

import com.yeginamgim.board.dto.BoardDto;
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

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // DB의 board 정보와 CSV의 장소 상세 정보를 합쳐 보드 상세 응답을 만든다.
    public BoardDto.BoardDetailResponse getBoardDetail(Long boardId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "보드를 찾을 수 없습니다."));

        BoardDto.PlaceInfo place = findPlaceByKakaoPlaceId(board.getKakaoPlaceId());

        return new BoardDto.BoardDetailResponse(
                board.getBoardId(),
                board.getKakaoPlaceId(),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                place
        );
    }

    // kakao_place_id를 기준으로 places.csv에서 장소 상세 정보를 찾는다.
    private BoardDto.PlaceInfo findPlaceByKakaoPlaceId(String kakaoPlaceId) {
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
                    return new BoardDto.PlaceInfo(
                            columns[0],
                            columns[1],
                            Double.parseDouble(columns[2]),
                            Double.parseDouble(columns[3]),
                            columns[4],
                            columns[5],
                            columns[6],
                            columns[7]
                    );
                }
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "장소 CSV 파일을 읽을 수 없습니다.");
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CSV에서 카카오 장소 정보를 찾을 수 없습니다.");
    }
}
