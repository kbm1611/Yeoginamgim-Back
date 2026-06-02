package com.yeginamgim.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void kakaoLocalApiExceptionReturnsBadGateway() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response = handler.handleKakaoLocalApi(
                new KakaoLocalApiException(new RuntimeException("kakao failed"))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isEqualTo("카카오 Local API 호출에 실패했습니다.");
    }

    @Test
    void invalidPlaceRequestExceptionReturnsBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response = handler.handleInvalidPlaceRequest(
                new InvalidPlaceRequestException("카테고리는 필수입니다.")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("카테고리는 필수입니다.");
    }

    @Test
    void placeNotFoundExceptionReturnsNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response = handler.handlePlaceNotFound(new PlaceNotFoundException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("로컬 장소 캐시에서 장소를 찾을 수 없습니다.");
    }

    @Test
    void loginFailedExceptionReturnsUnauthorized() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response = handler.handleLoginFailed(new LoginFailedException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
