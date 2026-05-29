package com.yeginamgim.global.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

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

    private String upload(MultipartFile uploadFile, Path uploadDir) {
        if (uploadFile == null || uploadFile.isEmpty()) return null;
        if (uploadFile.getSize() > MAX_FILE_SIZE) return null;

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
        } catch (IOException e) { System.out.println(e); }
        return null;
    }
}
