package com.yeginamgim.global.file;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileServiceTest {

    @Test
    void boardUploadAcceptsImageJpgContentTypeAsJpegAlias() {
        S3Service s3Service = mock(S3Service.class);
        FileService fileService = new FileService(s3Service, Path.of("build", "test-uploads"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trace.jpg",
                "image/jpg",
                jpegBytes()
        );
        when(s3Service.uploadFile(file, "board")).thenReturn("https://cdn.example.com/trace.jpg");

        String uploadedUrl = fileService.boardUpload(file);

        assertThat(uploadedUrl).isEqualTo("https://cdn.example.com/trace.jpg");
        verify(s3Service).uploadFile(file, "board");
    }

    private byte[] jpegBytes() {
        return new byte[] {
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF,
                0x00
        };
    }
}
