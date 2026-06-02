package com.yeginamgim.auth.jwt;

import com.yeginamgim.global.exception.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JWTServiceTest {

    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        ReflectionTestUtils.setField(jwtService, "secret", "12345678901234567890123456789012");
        jwtService.init();
    }

    @Test
    void getClaimReturnsEmailFromValidToken() {
        String token = jwtService.createToken("user@example.com");

        String email = jwtService.getClaim(token);

        assertThat(email).isEqualTo("user@example.com");
    }

    @Test
    void getClaimThrowsInvalidTokenExceptionForInvalidToken() {
        assertThatThrownBy(() -> jwtService.getClaim("invalid-token"))
                .isInstanceOf(InvalidTokenException.class);
    }
}
