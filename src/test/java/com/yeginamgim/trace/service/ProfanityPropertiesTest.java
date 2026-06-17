package com.yeginamgim.trace.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ProfanityPropertiesTest {

    @Test
    void getCheckUrlBuildsProfanityEndpointFromBaseUrl() {
        ProfanityProperties properties = new ProfanityProperties();
        properties.setBaseUrl("https://filter.example.com");

        assertThat(properties.getCheckUrl()).isEqualTo("https://filter.example.com/api/profanity");
    }

    @Test
    void getCheckUrlKeepsSingleSlashWhenBaseUrlEndsWithSlash() {
        ProfanityProperties properties = new ProfanityProperties();
        properties.setBaseUrl("https://filter.example.com/");

        assertThat(properties.getCheckUrl()).isEqualTo("https://filter.example.com/api/profanity");
    }

    @Test
    void localProfileDisablesProfanityFilterByDefaultAndKeepsLoopbackUrlForOptIn() throws IOException {
        Properties localProperties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("application-local.properties")) {
            assertThat(inputStream).isNotNull();
            localProperties.load(inputStream);
        }

        assertThat(localProperties.getProperty("profanity.filter.enabled"))
                .isEqualTo("${PROFANITY_FILTER_ENABLED:false}");
        assertThat(localProperties.getProperty("profanity.filter.base-url"))
                .isEqualTo("${PROFANITY_FILTER_BASE_URL:http://127.0.0.1:8001}");
        assertThat(localProperties.getProperty("profanity.filter.fail-open"))
                .isEqualTo("${PROFANITY_FILTER_FAIL_OPEN:false}");
    }
}
