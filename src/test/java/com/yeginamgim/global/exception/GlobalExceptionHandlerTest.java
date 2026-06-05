package com.yeginamgim.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void kakaoLocalApiExceptionReturnsBadGateway() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleKakaoLocalApi(
                new KakaoLocalApiException(new RuntimeException("kakao failed"))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().status()).isEqualTo(502);
    }

    @Test
    void emailVerificationMailExceptionReturnsBadGateway() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleEmailVerificationMail(
                new EmailVerificationMailException(new RuntimeException("mail failed"))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().code()).isEqualTo("EMAIL_VERIFICATION_MAIL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("이메일 인증번호 발송에 실패했습니다.");
        assertThat(response.getBody().status()).isEqualTo(502);
    }

    @Test
    void emailVerificationExceptionUsesExceptionStatusAndCode() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleEmailVerification(
                EmailVerificationException.cooldown()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody().code()).isEqualTo("EMAIL_VERIFICATION_COOLDOWN");
        assertThat(response.getBody().message()).isEqualTo("인증번호 재발송은 60초 후에 가능합니다.");
        assertThat(response.getBody().status()).isEqualTo(429);
    }

    @Test
    void invalidPlaceRequestExceptionReturnsBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleInvalidPlaceRequest(
                new InvalidPlaceRequestException("invalid place request")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_PLACE_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("invalid place request");
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    void placeNotFoundExceptionReturnsNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handlePlaceNotFound(new PlaceNotFoundException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("PLACE_NOT_FOUND");
    }

    @Test
    void loginFailedExceptionReturnsUnauthorized() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleLoginFailed(new LoginFailedException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().status()).isEqualTo(401);
    }

    @Test
    void invalidTokenExceptionReturnsUnauthorized() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleInvalidToken(new InvalidTokenException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("INVALID_TOKEN");
    }

    @Test
    void accountWithdrawalExceptionReturnsBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleAccountWithdrawal(
                new AccountWithdrawalException("invalid withdrawal")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("invalid withdrawal");
    }

    @Test
    void userNotFoundExceptionReturnsNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(new UserNotFoundException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("USER_NOT_FOUND");
    }

    @Test
    void responseStatusExceptionReturnsStandardJsonError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "missing resource")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("missing resource");
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    void fileUploadExceptionUsesExceptionStatusAndCode() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleFileUpload(
                FileUploadException.invalidImageFile()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_IMAGE_FILE");
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    void multipartExceptionReturnsStandardJsonBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> response = handler.handleMultipart(
                new MultipartException("broken multipart")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("MULTIPART_ERROR");
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    void bindValidationExceptionReturnsStandardJsonBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BindException exception = new BindException(new ValidationRequest(), "request");
        exception.rejectValue("email", "NotBlank", "email is required.");

        ResponseEntity<ErrorResponse> response = handler.handleBind(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).isEqualTo("email is required.");
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    private static class ValidationRequest {
        @SuppressWarnings("unused")
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
