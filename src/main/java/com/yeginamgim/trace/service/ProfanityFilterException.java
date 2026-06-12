package com.yeginamgim.trace.service;

class ProfanityFilterException extends RuntimeException {
    ProfanityFilterException(String message) {
        super(message);
    }

    ProfanityFilterException(String message, Throwable cause) {
        super(message, cause);
    }
}
