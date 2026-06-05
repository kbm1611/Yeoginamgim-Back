package com.yeginamgim.global.exception;

import org.springframework.http.HttpStatus;

public class EmailVerificationException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    private EmailVerificationException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static EmailVerificationException cooldown() {
        return new EmailVerificationException(
                "EMAIL_VERIFICATION_COOLDOWN",
                "인증번호 재발송은 60초 후에 가능합니다.",
                HttpStatus.TOO_MANY_REQUESTS
        );
    }

    public static EmailVerificationException expired() {
        return new EmailVerificationException(
                "EMAIL_VERIFICATION_EXPIRED",
                "인증번호가 만료되었습니다. 다시 요청해 주세요.",
                HttpStatus.BAD_REQUEST
        );
    }

    public static EmailVerificationException invalidCode() {
        return new EmailVerificationException(
                "EMAIL_VERIFICATION_INVALID_CODE",
                "인증번호가 일치하지 않습니다.",
                HttpStatus.BAD_REQUEST
        );
    }

    public static EmailVerificationException attemptsExceeded() {
        return new EmailVerificationException(
                "EMAIL_VERIFICATION_ATTEMPTS_EXCEEDED",
                "인증번호 입력 횟수를 초과했습니다. 다시 요청해 주세요.",
                HttpStatus.TOO_MANY_REQUESTS
        );
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
