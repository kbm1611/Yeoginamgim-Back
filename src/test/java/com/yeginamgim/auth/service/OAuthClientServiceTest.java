package com.yeginamgim.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthClientServiceTest {

    @Test
    void kakaoAuthorizeUrlIncludesState() {
        KakaoOAuthClientService service = new KakaoOAuthClientService();
        ReflectionTestUtils.setField(service, "kakaoClientId", "kakao-client-id");
        ReflectionTestUtils.setField(service, "kakaoRedirectUri", "http://localhost:8080/api/auth/oauth/kakao/callback");

        String loginUrl = service.getLoginUrl("state-123");

        assertThat(loginUrl).contains("state=state-123");
        assertThat(loginUrl).contains("client_id=kakao-client-id");
        assertThat(loginUrl).contains("redirect_uri=http://localhost:8080/api/auth/oauth/kakao/callback");
    }

    @Test
    void googleAuthorizeUrlIncludesState() {
        GoogleOAuthClientService service = new GoogleOAuthClientService();
        ReflectionTestUtils.setField(service, "googleClientId", "google-client-id");
        ReflectionTestUtils.setField(service, "googleRedirectUri", "http://localhost:8080/api/auth/oauth/google/callback");

        String loginUrl = service.getLoginUrl("state-123");

        assertThat(loginUrl).contains("state=state-123");
        assertThat(loginUrl).contains("client_id=google-client-id");
        assertThat(loginUrl).contains("redirect_uri=http://localhost:8080/api/auth/oauth/google/callback");
    }
}
