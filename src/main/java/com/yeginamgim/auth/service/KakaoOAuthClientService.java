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
public class KakaoOAuthClientService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    public String getLoginUrl() {
        return UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoClientId)
                .queryParam("redirect_uri", kakaoRedirectUri)
                .queryParam("scope", "profile_nickname profile_image account_email")
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
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    new HttpEntity<Void>(headers),
                    Map.class
            );
            body = response.getBody();
        } catch (RestClientException exception) {
            throw new OAuthLoginException();
        }

        Object id = getRequiredValue(body, "id");
        Map<String, Object> kakaoAccount = getRequiredMap(body, "kakao_account");
        Map<String, Object> profile = getRequiredMap(kakaoAccount, "profile");

        return new OAuthUserInfoDto(
                LoginProvider.KAKAO,
                String.valueOf(id),
                getRequiredString(kakaoAccount, "email"),
                getRequiredString(profile, "nickname"),
                (String) profile.get("profile_image_url")
        );
    }

    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);
        params.add("client_secret", kakaoClientSecret);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token",
                    new HttpEntity<>(params, headers),
                    Map.class
            );
            return getRequiredString(response.getBody(), "access_token");
        } catch (RestClientException exception) {
            throw new OAuthLoginException();
        }
    }

    private Map<String, Object> getRequiredMap(Map<String, Object> body, String key) {
        Object value = getRequiredValue(body, key);
        if (!(value instanceof Map)) {
            throw new OAuthLoginException();
        }
        return (Map<String, Object>) value;
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
