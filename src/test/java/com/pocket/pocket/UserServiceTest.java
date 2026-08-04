package com.pocket.pocket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class UserServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registersUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("plainTextPassword123");

        UserResponse response = userService.register(request);

        assertThat(response.getEmail()).isEqualTo("newuser@example.com");

        User stored = userRepository.findByEmail("newuser@example.com").orElseThrow();
        assertThat(stored.getPasswordHash()).isNotEqualTo("plainTextPassword123");
    }

    @Test
    void rejectsDuplicateEmail() {
        RegisterRequest first = new RegisterRequest();
        first.setEmail("duplicate@example.com");
        first.setPassword("password1");
        userService.register(first);

        RegisterRequest second = new RegisterRequest();
        second.setEmail("duplicate@example.com");
        second.setPassword("password2");

        assertThatThrownBy(() -> userService.register(second))
            .isInstanceOf(IllegalStateException.class);
    }
}