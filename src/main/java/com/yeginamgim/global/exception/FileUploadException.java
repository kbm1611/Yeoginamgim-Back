package com.yeginamgim.global.exception;

import org.springframework.http.HttpStatus;

public class FileUploadException extends RuntimeException {
    private static final String FILE_SIZE_EXCEEDED_MESSAGE = "File size must not exceed 5MB.";
    private static final String UNSUPPORTED_FILE_TYPE_MESSAGE = "Only JPEG, PNG, and WebP images can be uploaded.";
    private static final String INVALID_IMAGE_FILE_MESSAGE = "The uploaded file is not a valid image.";
    private static final String FILE_UPLOAD_FAILED_MESSAGE = "File upload failed.";

    private final String code;
    private final HttpStatus status;

    public FileUploadException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static FileUploadException fileTooLarge() {
        return new FileUploadException("FILE_TOO_LARGE", FILE_SIZE_EXCEEDED_MESSAGE, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    public static FileUploadException unsupportedFileType() {
        return new FileUploadException("UNSUPPORTED_FILE_TYPE", UNSUPPORTED_FILE_TYPE_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    public static FileUploadException invalidImageFile() {
        return new FileUploadException("INVALID_IMAGE_FILE", INVALID_IMAGE_FILE_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    public static FileUploadException uploadFailed() {
        return new FileUploadException("FILE_UPLOAD_FAILED", FILE_UPLOAD_FAILED_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
