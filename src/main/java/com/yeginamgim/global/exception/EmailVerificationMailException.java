package com.yeginamgim.global.exception;

public class EmailVerificationMailException extends RuntimeException {

    public EmailVerificationMailException(Throwable cause) {
        super("이메일 인증번호 발송에 실패했습니다.", cause);
    }
}
