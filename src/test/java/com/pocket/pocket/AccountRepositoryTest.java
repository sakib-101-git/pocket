package com.pocket.pocket;

import java.time.LocalDateTime;
import java.util.List;

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
class AccountRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void savesAndRetrievesAccountLinkedToUser() {
        User user = new User();
        user.setEmail("account-owner@example.com");
        user.setPasswordHash("hashed");
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setUser(savedUser);
        account.setName("Checking");
        account.setAccountType("checking");
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);

        List<Account> accounts = accountRepository.findByUserId(savedUser.getId());

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getName()).isEqualTo("Checking");
    }
}