package com.yeginamgim.global.file;

import com.yeginamgim.global.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

@Service
@Profile("local")
public class LocalFileStorageService implements FileStorageService {
    private static final String UPLOAD_URL_PREFIX = "/upload/";

    private final Path uploadRoot;

    @Autowired
    public LocalFileStorageService(@Value("${app.upload-root:uploads}") String uploadRoot) {
        this(Path.of(uploadRoot));
    }

    LocalFileStorageService(Path uploadRoot) {
        this.uploadRoot = uploadRoot.toAbsolutePath().normalize();
    }

    @Override
    public String upload(MultipartFile uploadFile, String directoryName, String fileName) {
        String safeDirectoryName = safeDirectoryName(directoryName);
        String safeFileName = safeFileName(fileName);
        Path uploadDir = uploadRoot.resolve(safeDirectoryName).normalize();
        Path uploadPath = uploadDir.resolve(safeFileName).normalize();

        if (!uploadDir.startsWith(uploadRoot) || !uploadPath.startsWith(uploadDir)) {
            throw FileUploadException.uploadFailed();
        }

        try {
            Files.createDirectories(uploadDir);
            uploadFile.transferTo(uploadPath.toFile());
            return UPLOAD_URL_PREFIX + safeDirectoryName + "/" + safeFileName;
        } catch (IOException | RuntimeException exception) {
            throw FileUploadException.uploadFailed();
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith(UPLOAD_URL_PREFIX)) {
            return;
        }

        String relativePath = fileUrl.substring(UPLOAD_URL_PREFIX.length());
        Path deletePath;

        try {
            deletePath = uploadRoot.resolve(relativePath).normalize();
        } catch (InvalidPathException exception) {
            return;
        }

        if (!deletePath.startsWith(uploadRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(deletePath);
        } catch (IOException exception) {
            return;
        }
    }

    private String safeDirectoryName(String directoryName) {
        if (directoryName == null || directoryName.isBlank()) {
            return "etc";
        }

        String sanitized = directoryName
                .replace('\\', '/')
                .replaceAll("[^A-Za-z0-9_-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        return sanitized.isBlank() ? "etc" : sanitized;
    }

    private String safeFileName(String fileName) {
        try {
            String safeFileName = Path.of(fileName).getFileName().toString();
            if (!safeFileName.isBlank()) {
                return safeFileName;
            }
        } catch (InvalidPathException exception) {
            return "file";
        }

        return "file";
    }
}
