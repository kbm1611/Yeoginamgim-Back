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
    }

    @Test
    void invalidPlaceRequestExceptionReturnsBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response = handler.handleInvalidPlaceRequest(
                new InvalidPlaceRequestException("invalid place request")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("invalid place request");
    }

    @Test
    void placeNotFoundExceptionReturnsNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response = handler.handlePlaceNotFound(new PlaceNotFoundException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void loginFailedExceptionReturnsUnauthorized() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response = handler.handleLoginFailed(new LoginFailedException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void invalidTokenExceptionReturnsUnauthorized() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response = handler.handleInvalidToken(new InvalidTokenException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
