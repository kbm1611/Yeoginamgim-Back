package com.yeginamgim.trace.service;

import org.junit.jupiter.api.Test;

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
}
