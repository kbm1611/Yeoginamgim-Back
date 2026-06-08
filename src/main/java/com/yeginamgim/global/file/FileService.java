package com.yeginamgim.global.file;

import com.yeginamgim.global.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String PROFILE_URL_PREFIX = "/upload/profile/";
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Path uploadRoot;
    private final Path profileUploadDir;
    private final Path boardUploadDir;
    private final S3Service s3Service;

    @Autowired
    public FileService(S3Service s3Service) {
        this(s3Service, Path.of(System.getProperty("user.dir"), "uploads"));
    }

    FileService(S3Service s3Service, Path uploadRoot) {
        this.s3Service = s3Service;
        this.uploadRoot = uploadRoot.toAbsolutePath().normalize();
        this.profileUploadDir = this.uploadRoot.resolve("profile").normalize();
        this.boardUploadDir = this.uploadRoot.resolve("board").normalize();
    }

    public String profileUpload(MultipartFile uploadFile) {
        return upload(uploadFile, profileUploadDir, "profile");
    }

    public String boardUpload(MultipartFile uploadFile) {
        return upload(uploadFile, boardUploadDir, "board");
    }

    public void deleteProfileFile(String profileImageUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) return;

        if (profileImageUrl.startsWith("http://") || profileImageUrl.startsWith("https://")) {
            s3Service.deleteFile(profileImageUrl);
            return;
        }

        if (!profileImageUrl.startsWith(PROFILE_URL_PREFIX)) return;

        String fileName = profileImageUrl.substring(PROFILE_URL_PREFIX.length());
        if (fileName.isBlank()) return;

        String safeFileName;
        try {
            safeFileName = Path.of(fileName).getFileName().toString();
        } catch (InvalidPathException e) {
            return;
        }
        Path deletePath = profileUploadDir.resolve(safeFileName).normalize();

        if (!deletePath.startsWith(profileUploadDir)) return;

        try {
            Files.deleteIfExists(deletePath);
        } catch (IOException e) {
            return;
        }
    }

    private String upload(MultipartFile uploadFile, Path uploadDir, String s3DirectoryName) {
        if (uploadFile == null || uploadFile.isEmpty()) return null;
        if (uploadFile.getSize() > MAX_FILE_SIZE) throw FileUploadException.fileTooLarge();

        String contentType = normalizeContentType(uploadFile.getContentType());
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw FileUploadException.unsupportedFileType();
        }
        validateImageSignature(uploadFile, contentType);

        String fileName = UUID.randomUUID() + "_" + safeOriginalFilename(uploadFile.getOriginalFilename());
        Path safeUploadDir = uploadDir.toAbsolutePath().normalize();
        Path uploadRealPath = safeUploadDir.resolve(fileName).normalize();

        if (!uploadRealPath.startsWith(safeUploadDir)) {
            throw FileUploadException.uploadFailed();
        }

        try {
//            Files.createDirectories(safeUploadDir);
//            uploadFile.transferTo(uploadRealPath.toFile());
//            return fileName;
            return s3Service.uploadFile(uploadFile, s3DirectoryName);
        } catch (RuntimeException e) {
            throw FileUploadException.uploadFailed();
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private void validateImageSignature(MultipartFile uploadFile, String contentType) {
        byte[] header = new byte[12];
        int read;

        try (InputStream inputStream = uploadFile.getInputStream()) {
            read = readHeader(inputStream, header);
        } catch (IOException e) {
            throw FileUploadException.invalidImageFile();
        }

        if (!hasImageSignature(header, read, contentType)) {
            throw FileUploadException.invalidImageFile();
        }
    }

    private int readHeader(InputStream inputStream, byte[] header) throws IOException {
        int totalRead = 0;
        while (totalRead < header.length) {
            int currentRead = inputStream.read(header, totalRead, header.length - totalRead);
            if (currentRead == -1) {
                break;
            }
            totalRead += currentRead;
        }
        return totalRead;
    }

    private boolean hasImageSignature(byte[] header, int read, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> read >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;
            case "image/png" -> read >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47
                    && header[4] == 0x0D
                    && header[5] == 0x0A
                    && header[6] == 0x1A
                    && header[7] == 0x0A;
            case "image/webp" -> read >= 12
                    && header[0] == 0x52
                    && header[1] == 0x49
                    && header[2] == 0x46
                    && header[3] == 0x46
                    && header[8] == 0x57
                    && header[9] == 0x45
                    && header[10] == 0x42
                    && header[11] == 0x50;
            default -> false;
        };
    }

    private String safeOriginalFilename(String originalFilename) {
        String baseName = "file";

        if (originalFilename != null && !originalFilename.isBlank()) {
            try {
                String normalizedSeparators = originalFilename.replace('\\', '/');
                String rawBaseName = normalizedSeparators.substring(normalizedSeparators.lastIndexOf('/') + 1);
                baseName = Path.of(rawBaseName).getFileName().toString();
            } catch (InvalidPathException e) {
                baseName = "file";
            }
        }

        String sanitized = baseName
                .replaceAll("[^A-Za-z0-9._-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^[.\\-]+", "")
                .replaceAll("[.\\-]+$", "");

        return sanitized.isBlank() ? "file" : sanitized;
    }
}
