package com.yeginamgim.global.file;

import com.yeginamgim.global.exception.FileUploadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileServiceTest {

    @TempDir
    Path uploadRoot;

    @Test
    void profileUploadStoresAllowedPngWithSafeOriginalFileNameUnderUploadRoot() {
        FileService fileService = new FileService(uploadRoot);
        MockMultipartFile file = new MockMultipartFile(
                "profileUploadFile",
                "../../profile image.png",
                "image/png",
                pngBytes()
        );

        String fileName = fileService.profileUpload(file);

        assertThat(fileName).endsWith("_profile-image.png");
        assertThat(fileName).doesNotContain("..", "/", "\\");
        assertThat(Files.exists(uploadRoot.resolve("profile").resolve(fileName))).isTrue();
    }

    @Test
    void boardUploadStoresAllowedWebpUnderUploadRoot() {
        FileService fileService = new FileService(uploadRoot);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trace.webp",
                "image/webp",
                webpBytes()
        );

        String fileName = fileService.boardUpload(file);

        assertThat(fileName).endsWith("_trace.webp");
        assertThat(Files.exists(uploadRoot.resolve("board").resolve(fileName))).isTrue();
    }

    @Test
    void uploadRejectsUnsupportedContentTypeAsBadRequest() {
        FileService fileService = new FileService(uploadRoot);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "note.txt",
                "text/plain",
                "not an image".getBytes()
        );

        assertThatThrownBy(() -> fileService.boardUpload(file))
                .isInstanceOfSatisfying(FileUploadException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getCode()).isEqualTo("UNSUPPORTED_FILE_TYPE");
                });
    }

    @Test
    void uploadRejectsImageContentTypeWithInvalidSignatureAsBadRequest() {
        FileService fileService = new FileService(uploadRoot);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.png",
                "image/png",
                "not an image".getBytes()
        );

        assertThatThrownBy(() -> fileService.boardUpload(file))
                .isInstanceOfSatisfying(FileUploadException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getCode()).isEqualTo("INVALID_IMAGE_FILE");
                });
    }

    @Test
    void uploadRejectsOversizedFileAsPayloadTooLarge() {
        FileService fileService = new FileService(uploadRoot);
        byte[] oversized = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.png",
                "image/png",
                oversized
        );

        assertThatThrownBy(() -> fileService.boardUpload(file))
                .isInstanceOfSatisfying(FileUploadException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
                    assertThat(exception.getCode()).isEqualTo("FILE_TOO_LARGE");
                });
    }

    private byte[] pngBytes() {
        return new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D
        };
    }

    private byte[] webpBytes() {
        return new byte[] {
                0x52, 0x49, 0x46, 0x46,
                0x00, 0x00, 0x00, 0x00,
                0x57, 0x45, 0x42, 0x50
        };
    }
}
