package com.yeginamgim.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class FileUploadExceptionTest {

    @Test
    void fileTooLargeUsesTenMegabyteLimitMessage() {
        FileUploadException exception = FileUploadException.fileTooLarge();

        assertThat(exception.getCode()).isEqualTo("FILE_TOO_LARGE");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(exception.getMessage()).isEqualTo("File size must not exceed 10MB.");
    }
}
