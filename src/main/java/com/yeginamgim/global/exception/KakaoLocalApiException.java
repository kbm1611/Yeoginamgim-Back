package com.yeginamgim.global.exception;

public class KakaoLocalApiException extends RuntimeException {

    public KakaoLocalApiException(Throwable cause) {
        super("카카오 Local API 호출에 실패했습니다.", cause);
    }
}
