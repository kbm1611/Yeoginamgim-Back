package com.yeginamgim.place.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
@ConditionalOnProperty(name = "place.cache.storage", havingValue = "local", matchIfMissing = true)
public class LocalPlaceCacheStorage implements PlaceCacheStorage {

    private final Path cacheFilePath;

    public LocalPlaceCacheStorage(@Value("${place.cache-file-path:../data/places-cache.csv}") String cacheFilePath) {
        this.cacheFilePath = Path.of(cacheFilePath).toAbsolutePath().normalize();
    }

    @Override
    public String read() {
        try {
            return Files.readString(cacheFilePath, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read place cache CSV.", exception);
        }
    }

    @Override
    public void write(String content) {
        try {
            Path parent = cacheFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Path tempPath = cacheFilePath.resolveSibling(cacheFilePath.getFileName() + ".tmp");
            Files.writeString(tempPath, content, StandardCharsets.UTF_8);
            Files.move(tempPath, cacheFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write place cache CSV.", exception);
        }
    }

    @Override
    public void ensureExists(String initialContent) {
        try {
            Path parent = cacheFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(cacheFilePath)) {
                Files.writeString(cacheFilePath, initialContent, StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize place cache CSV.", exception);
        }
    }
}
