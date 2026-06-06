package com.yeginamgim.auth.service;

import com.yeginamgim.auth.dto.OAuthUserInfoDto;
import com.yeginamgim.global.exception.OAuthLoginException;
import com.yeginamgim.user.enums.LoginProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class GoogleOAuthClientService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    public String getLoginUrl() {
        return getLoginUrl(null);
    }

    public String getLoginUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUri)
                .queryParam("scope", "openid email profile")
                .queryParamIfPresent("state", java.util.Optional.ofNullable(state).filter(value -> !value.isBlank()))
                .build()
                .toUriString();
    }

    public OAuthUserInfoDto fetchUserInfo(String code) {
        String accessToken = getAccessToken(code);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        Map<String, Object> body;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://openidconnect.googleapis.com/v1/userinfo",
                    HttpMethod.GET,
                    new HttpEntity<Void>(headers),
                    Map.class
            );
            body = response.getBody();
        } catch (RestClientException exception) {
            throw new OAuthLoginException();
        }

        return new OAuthUserInfoDto(
                LoginProvider.GOOGLE,
                getRequiredString(body, "sub"),
                getRequiredString(body, "email"),
                getRequiredString(body, "name"),
                (String) body.get("picture")
        );
    }

    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("code", code);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token",
                    new HttpEntity<>(params, headers),
                    Map.class
            );
            return getRequiredString(response.getBody(), "access_token");
        } catch (RestClientException exception) {
            throw new OAuthLoginException();
        }
    }

    private String getRequiredString(Map<String, Object> body, String key) {
        Object value = getRequiredValue(body, key);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            throw new OAuthLoginException();
        }
        return stringValue;
    }

    private Object getRequiredValue(Map<String, Object> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) {
            throw new OAuthLoginException();
        }
        return body.get(key);
    }
}
