package com.yeginamgim.global.file;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class FileWebConfig implements WebMvcConfigurer {

    private final Path uploadRoot = Path.of(System.getProperty("user.dir"), "uploads")
            .toAbsolutePath()
            .normalize();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(uploadRoot.toUri().toString());
    }
}
