package com.yeginamgim.trace.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "profanity.filter")
public class ProfanityProperties {
    private String baseUrl;
    private String checkPath = "/api/profanity";
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(5);
    private boolean enabled = true;
    private boolean failOpen = false;

    public String getCheckUrl() {
        if (!StringUtils.hasText(baseUrl)) {
            return "";
        }

        String normalizedBaseUrl = baseUrl.trim().replaceAll("/+$", "");
        String normalizedCheckPath = StringUtils.hasText(checkPath) ? checkPath.trim() : "/api/profanity";
        if (!normalizedCheckPath.startsWith("/")) {
            normalizedCheckPath = "/" + normalizedCheckPath;
        }

        return normalizedBaseUrl + normalizedCheckPath;
    }
}
