package com.yeginamgim.global.file;

import com.yeginamgim.global.exception.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String FILE_SIZE_EXCEEDED_MESSAGE = "파일 크기는 5MB를 초과할 수 없습니다.";
    private static final String PROFILE_URL_PREFIX = "/upload/profile/";

    private final Path uploadRoot = Path.of(System.getProperty("user.dir"), "uploads");
    private final Path profileUploadDir = uploadRoot.resolve("profile");
    private final Path boardUploadDir = uploadRoot.resolve("board");

    // [1] 프로필 업로드
    public String profileUpload(MultipartFile uploadFile){
        return upload(uploadFile, profileUploadDir);
    }

    // [2] 보드이미지 업로드
    public String boardUpload(MultipartFile uploadFile){
        return upload(uploadFile, boardUploadDir);
    }

    public void deleteProfileFile(String profileImageUrl) {
        if (profileImageUrl == null || !profileImageUrl.startsWith(PROFILE_URL_PREFIX)) return;

        String fileName = profileImageUrl.substring(PROFILE_URL_PREFIX.length());
        if (fileName.isBlank()) return;

        String safeFileName = Path.of(fileName).getFileName().toString();
        Path deletePath = profileUploadDir.resolve(safeFileName).normalize();

        if (!deletePath.startsWith(profileUploadDir)) return;

        try {
            Files.deleteIfExists(deletePath);
        } catch (IOException e) {
            return;
        }
    }

    private String upload(MultipartFile uploadFile, Path uploadDir) {
        if (uploadFile == null || uploadFile.isEmpty()) return null;
        if (uploadFile.getSize() > MAX_FILE_SIZE) throw new FileUploadException(FILE_SIZE_EXCEEDED_MESSAGE);

        String uuid = UUID.randomUUID().toString();
        String originalFilename = uploadFile.getOriginalFilename();
        String safeOriginalFilename = originalFilename == null ? "file" : Path.of(originalFilename).getFileName().toString().replaceAll("_", "-");
        String fileName = uuid + "_" + safeOriginalFilename;
        Path uploadRealPath = uploadDir.resolve(fileName).normalize();

        if (!uploadRealPath.startsWith(uploadDir)) return null;

        try {
            Files.createDirectories(uploadDir);
            uploadFile.transferTo(uploadRealPath.toFile());
            return fileName;
        } catch (IOException e) {
            throw new FileUploadException("file upload failed.");
        }
    }
}
