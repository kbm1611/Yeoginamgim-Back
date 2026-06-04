package com.yeginamgim.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String FILE_SIZE_EXCEEDED_MESSAGE = "파일 크기는 5MB를 초과할 수 없습니다.";

    @ExceptionHandler(DuplicateMemberException.class)
    public ResponseEntity<String> handleDuplicateMember(DuplicateMemberException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<String> handleFileUpload(FileUploadException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
    }

    @ExceptionHandler(OAuthLoginException.class)
    public ResponseEntity<String> handleOAuthLogin(OAuthLoginException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<String> handleLoginFailed(LoginFailedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(InvalidBirthDateException.class)
    public ResponseEntity<String> handleInvalidBirthDate(InvalidBirthDateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(AccountWithdrawalException.class)
    public ResponseEntity<String> handleAccountWithdrawal(AccountWithdrawalException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(KakaoLocalApiException.class)
    public ResponseEntity<String> handleKakaoLocalApi(KakaoLocalApiException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
    }

    @ExceptionHandler(InvalidPlaceRequestException.class)
    public ResponseEntity<String> handleInvalidPlaceRequest(InvalidPlaceRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(PlaceNotFoundException.class)
    public ResponseEntity<String> handlePlaceNotFound(PlaceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(firstValidationMessage(e));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleBind(BindException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(firstValidationMessage(e));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(FILE_SIZE_EXCEEDED_MESSAGE);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleMultipart(MultipartException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(FILE_SIZE_EXCEEDED_MESSAGE);
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
}
