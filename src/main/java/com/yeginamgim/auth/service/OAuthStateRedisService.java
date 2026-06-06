package com.yeginamgim.auth.service;

import com.yeginamgim.user.enums.LoginProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthStateRedisService {
    public static final Duration STATE_TTL = Duration.ofMinutes(5);

    private static final String STATE_KEY_PREFIX = "oauth:state:";
    private static final int STATE_BYTES = 32;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateState() {
        byte[] randomBytes = new byte[STATE_BYTES];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public void storeState(String state, LoginProvider provider) {
        redisTemplate.opsForValue().set(stateKey(state), provider.name(), STATE_TTL);
    }

    public Optional<LoginProvider> findProvider(String state) {
        String storedProvider = redisTemplate.opsForValue().get(stateKey(state));
        if (storedProvider == null || storedProvider.isBlank()) {
            return Optional.empty();
        }

        try {
            LoginProvider provider = LoginProvider.valueOf(storedProvider);
            if (provider == LoginProvider.LOCAL) {
                return Optional.empty();
            }
            return Optional.of(provider);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public void deleteState(String state) {
        redisTemplate.delete(stateKey(state));
    }

    private String stateKey(String state) {
        return STATE_KEY_PREFIX + state;
    }
}
