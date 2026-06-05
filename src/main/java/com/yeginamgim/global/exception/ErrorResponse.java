package com.yeginamgim.global.exception;

public record ErrorResponse(String code, String message, int status) {
}
