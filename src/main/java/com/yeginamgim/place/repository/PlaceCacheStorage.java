package com.yeginamgim.place.repository;

public interface PlaceCacheStorage {

    String read();

    void write(String content);

    void ensureExists(String initialContent);
}
