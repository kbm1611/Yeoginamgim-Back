package com.yeginamgim.auth.service;

import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.global.exception.LoginFailedException;
import com.yeginamgim.global.exception.OAuthLoginException;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final JWTService jwtSvc;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    // 일반 회원 로그인
    public LoginResponseDto login(LoginRequestDto loginReqDto){
        UserEntity userEntity = userRepo.findByEmail(loginReqDto.getEmail()).orElse(null);

        if(userEntity == null) throw new LoginFailedException();

        boolean isPasswordMatch = passwordEncoder.matches(
                loginReqDto.getPassword(),
                userEntity.getPassword()
        );

        if(!isPasswordMatch) throw new LoginFailedException();

        String token = jwtSvc.createToken(userEntity.getEmail());

        return LoginResponseDto.from(userEntity, token);
    }

    // 카카오 로그인 url
    public String getKaKaoLoginUrl(){
        return UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoClientId)
                .queryParam("redirect_uri", kakaoRedirectUri)
                .queryParam("scope", "profile_nickname profile_image account_email")
                .build()
                .toUriString();
    }

    // 카카오 로그인
    public LoginResponseDto kakaoLogin(String code) {
        String accessToken = getKakaoAccessToken( code );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth( accessToken );

        HttpEntity<Void> request = new HttpEntity<>( headers );

        Map<String, Object> body;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    request,
                    Map.class
            );
            body = response.getBody();
        } catch (RestClientException e) {
            throw new OAuthLoginException();
        }

        Object id = getRequiredValue(body, "id");
        String providerId = String.valueOf(id);

        Map<String, Object> kakaoAccount = getRequiredMap(body, "kakao_account");
        Map<String, Object> profile = getRequiredMap(kakaoAccount, "profile");

        String email = getRequiredString(kakaoAccount, "email");
        String nickname = getRequiredString(profile, "nickname");
        String profileImageUrl = (String) profile.get( "profile_image_url" );

        return socialLogin(
                LoginProvider.KAKAO,
                providerId,
                email,
                nickname,
                profileImageUrl
        );
    }

    // 카카오 엑세스 토큰 얻기
    private String getKakaoAccessToken( String code ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "grant_type", "authorization_code" );
        params.add( "client_id", kakaoClientId);
        params.add( "redirect_uri", kakaoRedirectUri);
        params.add( "code", code);
        params.add( "client_secret", kakaoClientSecret);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token",
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            return getRequiredString(body, "access_token");
        } catch (RestClientException e) {
            throw new OAuthLoginException();
        }
    }

    // 구글 로그인 url 생성
    public String getGoogleLoginUrl() {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUri)
                .queryParam("scope", "openid email profile")
                .build()
                .toUriString();
    }

    // 구글 로그인
    public LoginResponseDto googleLogin(String code) {
        String accessToken = getGoogleAccessToken(code);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        Map<String, Object> body;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://openidconnect.googleapis.com/v1/userinfo",
                    HttpMethod.GET,
                    request,
                    Map.class
            );
            body = response.getBody();
        } catch (RestClientException e) {
            throw new OAuthLoginException();
        }

        String providerId = getRequiredString(body, "sub");
        String email = getRequiredString(body, "email");
        String nickname = getRequiredString(body, "name");
        String profileImageUrl = (String) body.get("picture");

        return socialLogin(
                LoginProvider.GOOGLE,
                providerId,
                email,
                nickname,
                profileImageUrl
        );
    }

    // 구글 엑세스토큰 얻기
    private String getGoogleAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token",
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            return getRequiredString(body, "access_token");
        } catch (RestClientException e) {
            throw new OAuthLoginException();
        }
    }

    // 소셜 로그인
    public LoginResponseDto socialLogin(
            LoginProvider provider,
            String providerId,
            String email,
            String nickname,
            String profileImageUrl
    ){
        if (providerId == null || email == null || nickname == null) {
            throw new OAuthLoginException();
        }

        UserEntity userEntity = userRepo.findByProviderAndProviderId(provider, providerId).orElse(null);

        if( userEntity == null ){
            userEntity = userRepo.findByEmail(email).orElse(null);

            if( userEntity == null ){
                userEntity = UserEntity.builder()
                        .email(email)
                        .nickname(nickname)
                        .profileImageUrl(profileImageUrl)
                        .provider(provider)
                        .providerId(providerId)
                        .build();

                userEntity = userRepo.save(userEntity);
            }
        }

        String token = jwtSvc.createToken(userEntity.getEmail());

        return LoginResponseDto.from(userEntity, token);
    }

    private Map<String, Object> getRequiredMap(Map<String, Object> body, String key) {
        Object value = getRequiredValue(body, key);
        if (!(value instanceof Map)) throw new OAuthLoginException();
        return (Map<String, Object>) value;
    }

    private String getRequiredString(Map<String, Object> body, String key) {
        Object value = getRequiredValue(body, key);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) throw new OAuthLoginException();
        return stringValue;
    }

    private Object getRequiredValue(Map<String, Object> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) throw new OAuthLoginException();
        return body.get(key);
    }

}
