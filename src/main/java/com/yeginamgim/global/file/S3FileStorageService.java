package com.yeginamgim.global.file;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("!local")
public class S3FileStorageService implements FileStorageService {

    private final S3Service s3Service;

    public S3FileStorageService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Override
    public String upload(MultipartFile uploadFile, String directoryName, String fileName) {
        return s3Service.uploadFile(uploadFile, directoryName);
    }

    @Override
    public void delete(String fileUrl) {
        s3Service.deleteFile(fileUrl);
    }
}
