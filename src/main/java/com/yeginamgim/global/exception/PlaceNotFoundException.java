package com.yeginamgim.global.exception;

public class PlaceNotFoundException extends RuntimeException {

    public PlaceNotFoundException() {
        super("로컬 장소 캐시에서 장소를 찾을 수 없습니다.");
    }
}
