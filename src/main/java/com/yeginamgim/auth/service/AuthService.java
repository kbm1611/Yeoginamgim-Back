package com.yeginamgim.auth.service;

import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.auth.dto.OAuthUserInfoDto;
import com.yeginamgim.global.exception.LoginFailedException;
import com.yeginamgim.global.exception.OAuthLoginException;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final JWTService jwtSvc;
    private final KakaoOAuthClientService kakaoOAuthClientService;
    private final GoogleOAuthClientService googleOAuthClientService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponseDto login(LoginRequestDto loginReqDto) {
        UserEntity userEntity = userRepo.findByEmail(loginReqDto.getEmail())
                .orElseThrow(LoginFailedException::new);

        boolean isPasswordMatch = passwordEncoder.matches(
                loginReqDto.getPassword(),
                userEntity.getPassword()
        );

        if (!isPasswordMatch) {
            throw new LoginFailedException();
        }

        String token = jwtSvc.createToken(userEntity.getEmail());
        return LoginResponseDto.from(userEntity, token);
    }

    public String getKaKaoLoginUrl() {
        return kakaoOAuthClientService.getLoginUrl();
    }

    public LoginResponseDto kakaoLogin(String code) {
        return socialLogin(kakaoOAuthClientService.fetchUserInfo(code));
    }

    public String getGoogleLoginUrl() {
        return googleOAuthClientService.getLoginUrl();
    }

    public LoginResponseDto googleLogin(String code) {
        return socialLogin(googleOAuthClientService.fetchUserInfo(code));
    }

    private LoginResponseDto socialLogin(OAuthUserInfoDto userInfo) {
        return socialLogin(
                userInfo.provider(),
                userInfo.providerId(),
                userInfo.email(),
                userInfo.nickname(),
                userInfo.profileImageUrl()
        );
    }

    public LoginResponseDto socialLogin(
            LoginProvider provider,
            String providerId,
            String email,
            String nickname,
            String profileImageUrl
    ) {
        if (providerId == null || email == null || nickname == null) {
            throw new OAuthLoginException();
        }

        UserEntity userEntity = userRepo.findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        if (userEntity == null) {
            userEntity = userRepo.findByEmail(email).orElse(null);

            if (userEntity == null) {
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
}
