package com.yeginamgim.global.exception;

public class InvalidBirthDateException extends RuntimeException {
    public InvalidBirthDateException() {
        super("birthDate must be a valid YYMMDD date.");
    }
}
