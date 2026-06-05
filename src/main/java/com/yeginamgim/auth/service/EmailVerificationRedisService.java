package com.yeginamgim.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailVerificationRedisService {
    private static final String CODE_KEY_PREFIX = "email:verify:code:";
    private static final String VERIFIED_KEY_PREFIX = "email:verify:ok:";
    private static final String COOLDOWN_KEY_PREFIX = "email:verify:cooldown:";
    private static final String ATTEMPTS_KEY_PREFIX = "email:verify:attempts:";

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    private static final Duration ATTEMPTS_TTL = Duration.ofMinutes(5);

    private static final String ACTIVE_VALUE = "1";

    private final StringRedisTemplate redisTemplate;

    public void storeVerificationCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);

        redisTemplate.opsForValue().set(codeKey(normalizedEmail), hashCode(code), CODE_TTL);
        redisTemplate.opsForValue().set(cooldownKey(normalizedEmail), ACTIVE_VALUE, COOLDOWN_TTL);
        redisTemplate.delete(attemptsKey(normalizedEmail));
        redisTemplate.delete(verifiedKey(normalizedEmail));
    }

    public boolean verifyCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        String storedCodeHash = redisTemplate.opsForValue().get(codeKey(normalizedEmail));

        if (storedCodeHash != null && storedCodeHash.equals(hashCode(code))) {
            redisTemplate.opsForValue().set(verifiedKey(normalizedEmail), ACTIVE_VALUE, VERIFIED_TTL);
            redisTemplate.delete(Set.of(codeKey(normalizedEmail), attemptsKey(normalizedEmail)));
            return true;
        }

        recordFailedAttempt(normalizedEmail);
        return false;
    }

    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(verifiedKey(normalizeEmail(email))));
    }

    public boolean hasCooldown(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey(normalizeEmail(email))));
    }

    public void clearVerificationState(String email) {
        String normalizedEmail = normalizeEmail(email);
        redisTemplate.delete(Set.of(
                codeKey(normalizedEmail),
                verifiedKey(normalizedEmail),
                cooldownKey(normalizedEmail),
                attemptsKey(normalizedEmail)
        ));
    }

    private void recordFailedAttempt(String normalizedEmail) {
        String attemptsKey = attemptsKey(normalizedEmail);
        redisTemplate.opsForValue().increment(attemptsKey);
        redisTemplate.expire(attemptsKey, ATTEMPTS_TTL);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String hashCode(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(nullToEmpty(code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String codeKey(String email) {
        return CODE_KEY_PREFIX + email;
    }

    private String verifiedKey(String email) {
        return VERIFIED_KEY_PREFIX + email;
    }

    private String cooldownKey(String email) {
        return COOLDOWN_KEY_PREFIX + email;
    }

    private String attemptsKey(String email) {
        return ATTEMPTS_KEY_PREFIX + email;
    }
}
