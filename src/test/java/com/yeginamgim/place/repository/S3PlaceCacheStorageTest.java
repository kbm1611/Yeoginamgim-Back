package com.yeginamgim.place.repository;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3PlaceCacheStorageTest {

    @Test
    void readReturnsS3ObjectContentAsUtf8() {
        S3Client s3Client = mock(S3Client.class);
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(
                GetObjectResponse.builder().build(),
                "header\nrow\n".getBytes(StandardCharsets.UTF_8)
        );
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

        S3PlaceCacheStorage storage = new S3PlaceCacheStorage(s3Client, "bucket", "data/places-cache.csv");

        assertThat(storage.read()).isEqualTo("header\nrow\n");
    }

    @Test
    void ensureExistsCreatesObjectWhenKeyIsMissing() {
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).build());

        S3PlaceCacheStorage storage = new S3PlaceCacheStorage(s3Client, "bucket", "data/places-cache.csv");

        storage.ensureExists("header\n");

        verify(s3Client).putObject(
                argThat((PutObjectRequest request) ->
                        request.bucket().equals("bucket")
                                && request.key().equals("data/places-cache.csv")
                                && request.contentType().equals("text/csv; charset=UTF-8")),
                any(RequestBody.class)
        );
    }
}
