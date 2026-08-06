package com.pocket.pocket;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwtService =
        new JwtService("test-secret-key-that-is-long-enough-for-hs256", 900000);

    @Test
    void generatesAndValidatesToken() {
        String token = jwtService.generateToken(1L, "test@example.com");

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    void rejectsGarbageToken() {
        assertThat(jwtService.isTokenValid("not-a-real-token")).isFalse();
    }
}