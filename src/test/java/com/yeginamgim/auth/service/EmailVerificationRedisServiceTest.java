package com.yeginamgim.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationRedisServiceTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final EmailVerificationRedisService service = new EmailVerificationRedisService(redisTemplate);

    EmailVerificationRedisServiceTest() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void storeVerificationCodeNormalizesEmailAndStoresHashedCodeWithTtls() {
        service.storeVerificationCode("  USER@Example.COM  ", "123456");

        ArgumentCaptor<String> storedCode = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(
                eq("email:verify:code:user@example.com"),
                storedCode.capture(),
                eq(Duration.ofMinutes(5))
        );
        assertThat(storedCode.getValue()).isNotEqualTo("123456");
        assertThat(storedCode.getValue()).isNotBlank();
        verify(valueOperations).set(
                "email:verify:cooldown:user@example.com",
                "1",
                Duration.ofSeconds(60)
        );
        verify(redisTemplate).delete("email:verify:attempts:user@example.com");
        verify(redisTemplate).delete("email:verify:ok:user@example.com");
    }

    @Test
    void verifyCodeStoresSuccessStateAndDeletesTemporaryStateWhenCodeMatches() {
        service.storeVerificationCode("user@example.com", "123456");
        ArgumentCaptor<String> storedCode = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(
                eq("email:verify:code:user@example.com"),
                storedCode.capture(),
                eq(Duration.ofMinutes(5))
        );
        when(valueOperations.get("email:verify:code:user@example.com")).thenReturn(storedCode.getValue());

        boolean verified = service.verifyCode(" USER@example.COM ", "123456");

        assertThat(verified).isTrue();
        verify(valueOperations).set("email:verify:ok:user@example.com", "1", Duration.ofMinutes(30));
        verify(redisTemplate).delete(Set.of(
                "email:verify:code:user@example.com",
                "email:verify:attempts:user@example.com"
        ));
    }

    @Test
    void verifyCodeIncrementsAttemptsWithTtlWhenCodeDoesNotMatch() {
        when(valueOperations.get("email:verify:code:user@example.com")).thenReturn("stored-hash");

        boolean verified = service.verifyCode("user@example.com", "000000");

        assertThat(verified).isFalse();
        verify(valueOperations).increment("email:verify:attempts:user@example.com");
        verify(redisTemplate).expire("email:verify:attempts:user@example.com", Duration.ofMinutes(5));
        verify(valueOperations, never()).set(eq("email:verify:ok:user@example.com"), any(), any(Duration.class));
    }

    @Test
    void isVerifiedAndHasCooldownCheckNormalizedKeys() {
        when(redisTemplate.hasKey("email:verify:ok:user@example.com")).thenReturn(true);
        when(redisTemplate.hasKey("email:verify:cooldown:user@example.com")).thenReturn(false);

        assertThat(service.isVerified(" USER@example.COM ")).isTrue();
        assertThat(service.hasCooldown(" USER@example.COM ")).isFalse();
    }

    @Test
    void hasVerificationCodeAndGetFailedAttemptsUseNormalizedKeys() {
        when(redisTemplate.hasKey("email:verify:code:user@example.com")).thenReturn(true);
        when(valueOperations.get("email:verify:attempts:user@example.com")).thenReturn("3");

        assertThat(service.hasVerificationCode(" USER@example.COM ")).isTrue();
        assertThat(service.getFailedAttempts(" USER@example.COM ")).isEqualTo(3L);
    }

    @Test
    void clearVerificationStateDeletesAllVerificationKeys() {
        service.clearVerificationState(" USER@example.COM ");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> keys = ArgumentCaptor.forClass(Collection.class);
        verify(redisTemplate).delete(keys.capture());
        assertThat(keys.getValue()).containsExactlyInAnyOrder(
                "email:verify:code:user@example.com",
                "email:verify:ok:user@example.com",
                "email:verify:cooldown:user@example.com",
                "email:verify:attempts:user@example.com"
        );
    }
}
