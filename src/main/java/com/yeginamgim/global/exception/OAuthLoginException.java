package com.yeginamgim.global.exception;

public class OAuthLoginException extends RuntimeException {
    public OAuthLoginException() {
        super("소셜 로그인에 실패했습니다.");
    }
}
