package com.yeginamgim.auth.service;

import com.yeginamgim.auth.dto.request.EmailVerificationSendRequest;
import com.yeginamgim.auth.dto.request.EmailVerificationVerifyRequest;
import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.EmailVerificationResponse;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.auth.dto.OAuthUserInfoDto;
import com.yeginamgim.global.exception.DuplicateMemberException;
import com.yeginamgim.global.exception.EmailVerificationException;
import com.yeginamgim.global.exception.EmailVerificationMailException;
import com.yeginamgim.global.exception.LoginFailedException;
import com.yeginamgim.global.exception.OAuthLoginException;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Duration EMAIL_VERIFICATION_CODE_TTL = Duration.ofMinutes(5);
    private static final long MAX_EMAIL_VERIFICATION_ATTEMPTS = 5L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepo;
    private final JWTService jwtSvc;
    private final KakaoOAuthClientService kakaoOAuthClientService;
    private final GoogleOAuthClientService googleOAuthClientService;
    private final EmailVerificationRedisService emailVerificationRedisService;
    private final OAuthStateRedisService oauthStateRedisService;
    private final MailService mailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto loginReqDto) {
        UserEntity userEntity = userRepo.findByEmail(loginReqDto.getEmail())
                .orElseThrow(LoginFailedException::new);

        if (userEntity.getProvider() != LoginProvider.LOCAL || userEntity.getPassword() == null) {
            throw new LoginFailedException();
        }

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

    @Transactional(readOnly = true)
    public EmailVerificationResponse sendEmailVerification(EmailVerificationSendRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepo.findByEmail(email).isPresent()) {
            throw new DuplicateMemberException();
        }

        if (!emailVerificationRedisService.tryReserveCooldown(email)) {
            throw EmailVerificationException.cooldown();
        }

        String code = generateVerificationCode();
        emailVerificationRedisService.storeVerificationCode(email, code);
        try {
            mailService.sendVerificationCode(email, code, EMAIL_VERIFICATION_CODE_TTL);
        } catch (EmailVerificationMailException e) {
            emailVerificationRedisService.clearVerificationState(email);
            throw e;
        }

        return EmailVerificationResponse.builder()
                .message("인증번호가 이메일로 발송되었습니다.")
                .verified(false)
                .build();
    }

    public EmailVerificationResponse verifyEmailVerification(EmailVerificationVerifyRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (!emailVerificationRedisService.hasVerificationCode(email)) {
            throw EmailVerificationException.expired();
        }

        if (emailVerificationRedisService.getFailedAttempts(email) >= MAX_EMAIL_VERIFICATION_ATTEMPTS) {
            throw EmailVerificationException.attemptsExceeded();
        }

        if (!emailVerificationRedisService.verifyCode(email, request.getCode())) {
            if (emailVerificationRedisService.getFailedAttempts(email) >= MAX_EMAIL_VERIFICATION_ATTEMPTS) {
                throw EmailVerificationException.attemptsExceeded();
            }
            throw EmailVerificationException.invalidCode();
        }

        return EmailVerificationResponse.builder()
                .message("이메일 인증이 완료되었습니다.")
                .verified(true)
                .build();
    }

    public String getKaKaoLoginUrl() {
        return startKakaoOAuth().authorizationUrl();
    }

    public OAuthLoginStart startKakaoOAuth() {
        return startOAuth(LoginProvider.KAKAO);
    }

    @Transactional
    public LoginResponseDto kakaoLogin(String code) {
        return socialLogin(kakaoOAuthClientService.fetchUserInfo(code));
    }

    public String getGoogleLoginUrl() {
        return startGoogleOAuth().authorizationUrl();
    }

    public OAuthLoginStart startGoogleOAuth() {
        return startOAuth(LoginProvider.GOOGLE);
    }

    @Transactional
    public LoginResponseDto googleLogin(String code) {
        return socialLogin(googleOAuthClientService.fetchUserInfo(code));
    }

    public boolean consumeKakaoOAuthState(String state) {
        return consumeOAuthState(LoginProvider.KAKAO, state);
    }

    public boolean consumeGoogleOAuthState(String state) {
        return consumeOAuthState(LoginProvider.GOOGLE, state);
    }

    private OAuthLoginStart startOAuth(LoginProvider provider) {
        String state = oauthStateRedisService.generateState();
        oauthStateRedisService.storeState(state, provider);

        String authorizationUrl = provider == LoginProvider.KAKAO
                ? kakaoOAuthClientService.getLoginUrl(state)
                : googleOAuthClientService.getLoginUrl(state);

        return new OAuthLoginStart(authorizationUrl, state, OAuthStateRedisService.STATE_TTL);
    }

    private boolean consumeOAuthState(LoginProvider expectedProvider, String state) {
        if (state == null || state.isBlank()) {
            return false;
        }

        boolean valid = oauthStateRedisService.findProvider(state)
                .filter(expectedProvider::equals)
                .isPresent();
        if (!valid) {
            return false;
        }

        oauthStateRedisService.deleteState(state);
        return true;
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

    @Transactional
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

        if (userEntity != null && userEntity.isWithdrawn()) {
            throw new OAuthLoginException();
        }

        if (userEntity == null) {
            if (userRepo.findByEmail(email).isPresent()) {
                throw new DuplicateMemberException();
            }

            userEntity = UserEntity.builder()
                    .email(email)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .provider(provider)
                    .providerId(providerId)
                    .build();

            userEntity = userRepo.save(userEntity);
        }

        String token = jwtSvc.createToken(userEntity.getEmail());
        return LoginResponseDto.from(userEntity, token);
    }

    private String generateVerificationCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
