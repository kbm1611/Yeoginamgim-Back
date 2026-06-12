package com.yeginamgim.global.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String upload(MultipartFile uploadFile, String directoryName, String fileName);

    void delete(String fileUrl);
}
