package com.yeginamgim.auth.service;

import com.yeginamgim.user.enums.LoginProvider;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuthStateRedisServiceTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final OAuthStateRedisService service = new OAuthStateRedisService(redisTemplate);

    OAuthStateRedisServiceTest() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateStateCreatesUrlSafeUnpredictableValue() {
        String state = service.generateState();

        assertThat(state).matches("[A-Za-z0-9_-]{32,}");
    }

    @Test
    void storeStateStoresProviderWithTtl() {
        service.storeState("state-123", LoginProvider.KAKAO);

        verify(valueOperations).set(
                "oauth:state:state-123",
                "KAKAO",
                Duration.ofMinutes(5)
        );
    }

    @Test
    void findProviderReturnsStoredProvider() {
        when(valueOperations.get("oauth:state:state-123")).thenReturn("GOOGLE");

        Optional<LoginProvider> provider = service.findProvider("state-123");

        assertThat(provider).contains(LoginProvider.GOOGLE);
    }

    @Test
    void findProviderReturnsEmptyWhenStateIsMissingOrInvalid() {
        when(valueOperations.get("oauth:state:state-123")).thenReturn(null);
        when(valueOperations.get("oauth:state:bad-state")).thenReturn("LOCAL");

        assertThat(service.findProvider("state-123")).isEmpty();
        assertThat(service.findProvider("bad-state")).isEmpty();
    }

    @Test
    void deleteStateDeletesRedisKey() {
        service.deleteState("state-123");

        verify(redisTemplate).delete("oauth:state:state-123");
    }
}
