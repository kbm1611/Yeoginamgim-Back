package com.yeginamgim.global.exception;

public class LoginFailedException extends RuntimeException {

    public LoginFailedException() {
        super("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
