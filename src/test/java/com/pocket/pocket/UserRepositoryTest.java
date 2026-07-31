package com.pocket.pocket;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndRetrievesUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("some-hashed-value");
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        User found = userRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }
}
