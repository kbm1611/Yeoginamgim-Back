package com.yeginamgim.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final String CLOUDFRONT_FRONTEND_URL = "https://d2a908jq2crel3.cloudfront.net";

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        normalizeOrigin(frontendBaseUrl),
                        CLOUDFRONT_FRONTEND_URL,
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    private String normalizeOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return "http://localhost:5173";
        }

        String normalizedOrigin = origin.trim();
        return normalizedOrigin.endsWith("/")
                ? normalizedOrigin.substring(0, normalizedOrigin.length() - 1)
                : normalizedOrigin;
    }
}
