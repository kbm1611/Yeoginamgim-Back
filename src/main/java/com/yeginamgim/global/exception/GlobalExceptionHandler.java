package com.yeginamgim.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String FILE_SIZE_EXCEEDED_MESSAGE = "File size must not exceed 5MB.";

    @ExceptionHandler(DuplicateMemberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMember(DuplicateMemberException e) {
        return error(HttpStatus.CONFLICT, "DUPLICATE_MEMBER", e.getMessage());
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUpload(FileUploadException e) {
        return error(e.getStatus(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(OAuthLoginException.class)
    public ResponseEntity<ErrorResponse> handleOAuthLogin(OAuthLoginException e) {
        return error(HttpStatus.UNAUTHORIZED, "OAUTH_LOGIN_FAILED", e.getMessage());
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponse> handleLoginFailed(LoginFailedException e) {
        return error(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException e) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", e.getMessage());
    }

    @ExceptionHandler(InvalidBirthDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBirthDate(InvalidBirthDateException e) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_BIRTH_DATE", e.getMessage());
    }

    @ExceptionHandler(AccountWithdrawalException.class)
    public ResponseEntity<ErrorResponse> handleAccountWithdrawal(AccountWithdrawalException e) {
        return error(HttpStatus.BAD_REQUEST, "ACCOUNT_WITHDRAWAL_ERROR", e.getMessage());
    }

    @ExceptionHandler(KakaoLocalApiException.class)
    public ResponseEntity<ErrorResponse> handleKakaoLocalApi(KakaoLocalApiException e) {
        return error(HttpStatus.BAD_GATEWAY, "KAKAO_LOCAL_API_ERROR", e.getMessage());
    }

    @ExceptionHandler(EmailVerificationMailException.class)
    public ResponseEntity<ErrorResponse> handleEmailVerificationMail(EmailVerificationMailException e) {
        return error(HttpStatus.BAD_GATEWAY, "EMAIL_VERIFICATION_MAIL_ERROR", e.getMessage());
    }

    @ExceptionHandler(InvalidPlaceRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPlaceRequest(InvalidPlaceRequestException e) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_PLACE_REQUEST", e.getMessage());
    }

    @ExceptionHandler(PlaceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlaceNotFound(PlaceNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "PLACE_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", firstValidationMessage(e));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException e) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", firstValidationMessage(e));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", FILE_SIZE_EXCEEDED_MESSAGE);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipart(MultipartException e) {
        return error(HttpStatus.BAD_REQUEST, "MULTIPART_ERROR", "Invalid multipart request.");
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponse> handleServletRequestBinding(ServletRequestBindingException e) {
        return error(HttpStatus.BAD_REQUEST, "REQUEST_BINDING_ERROR", e.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
        HttpStatusCode statusCode = e.getStatusCode();
        String message = e.getReason() == null || e.getReason().isBlank()
                ? "Request failed."
                : e.getReason();
        return ResponseEntity.status(statusCode)
                .body(new ErrorResponse(errorCode(statusCode), message, statusCode.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error.");
    }

    private String firstValidationMessage(BindException e) {
        if (e.getBindingResult().hasFieldErrors()) {
            return e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        if (e.getBindingResult().hasGlobalErrors()) {
            return e.getBindingResult().getGlobalErrors().get(0).getDefaultMessage();
        }
        return "Invalid request.";
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, status.value()));
    }

    private String errorCode(HttpStatusCode statusCode) {
        if (statusCode instanceof HttpStatus status) {
            return status.name();
        }
        return "HTTP_" + statusCode.value();
    }
}
