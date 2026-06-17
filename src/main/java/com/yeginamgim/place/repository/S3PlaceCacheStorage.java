package com.yeginamgim.place.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(name = "place.cache.storage", havingValue = "s3")
public class S3PlaceCacheStorage implements PlaceCacheStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String key;

    public S3PlaceCacheStorage(
            S3Client s3Client,
            @Value("${place.cache.s3.bucket:${spring.cloud.aws.s3.bucket}}") String bucket,
            @Value("${place.cache.s3.key:data/places-cache.csv}") String key
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public String read() {
        return s3Client.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build())
                .asString(StandardCharsets.UTF_8);
    }

    @Override
    public void write(String content) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("text/csv; charset=UTF-8")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(content, StandardCharsets.UTF_8));
    }

    @Override
    public void ensureExists(String initialContent) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                write(initialContent);
                return;
            }
            throw exception;
        }
    }
}
