package com.yeginamgim.global.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class FileWebConfig implements WebMvcConfigurer {

    private final Path uploadRoot;

    public FileWebConfig(@Value("${app.upload-root:uploads}") String uploadRoot) {
        this.uploadRoot = Path.of(uploadRoot)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(uploadRoot.toUri().toString());
    }
}
