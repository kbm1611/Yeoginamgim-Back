package com.yeginamgim.global.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void boardUploadValidatesImageAndDelegatesToStorage() {
        FileStorageService storageService = mock(FileStorageService.class);
        FileService fileService = new FileService(storageService);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trace.jpg",
                "image/jpg",
                jpegBytes()
        );
        when(storageService.upload(eq(file), eq("board"), org.mockito.ArgumentMatchers.endsWith("_trace.jpg")))
                .thenReturn("https://cdn.example.com/trace.jpg");

        String uploadedUrl = fileService.boardUpload(file);

        assertThat(uploadedUrl).isEqualTo("https://cdn.example.com/trace.jpg");
        verify(storageService).upload(eq(file), eq("board"), org.mockito.ArgumentMatchers.endsWith("_trace.jpg"));
    }

    @Test
    void boardUploadRejectsInvalidImageBeforeCallingStorage() {
        FileStorageService storageService = mock(FileStorageService.class);
        FileService fileService = new FileService(storageService);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trace.png",
                "image/png",
                "not an image".getBytes()
        );

        assertThatThrownBy(() -> fileService.boardUpload(file))
                .isInstanceOf(com.yeginamgim.global.exception.FileUploadException.class);
        verifyNoInteractions(storageService);
    }

    @Test
    void localStorageStoresBoardFileUnderUploadRootAndReturnsUploadUrl() throws Exception {
        LocalFileStorageService storageService = new LocalFileStorageService(tempDir);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trace.png",
                "image/png",
                pngBytes()
        );

        String uploadedUrl = storageService.upload(file, "board", "safe-trace.png");

        assertThat(uploadedUrl).isEqualTo("/upload/board/safe-trace.png");
        assertThat(Files.exists(tempDir.resolve("board").resolve("safe-trace.png"))).isTrue();
    }

    private byte[] jpegBytes() {
        return new byte[] {
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF,
                0x00
        };
    }

    private byte[] pngBytes() {
        return new byte[] {
                (byte) 0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A,
                0x00,
                0x00,
                0x00,
                0x0D
        };
    }
}
