package com.pocket.pocket;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AccountSyncTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void upsertingSameTransactionTwiceDoesNotDuplicate() {
        User user = new User();
        user.setEmail("sync-test@example.com");
        user.setPasswordHash(passwordEncoder.encode("testpass123"));
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        Account account = new Account();
        account.setUser(user);
        account.setName("Sync Test");
        account.setAccountType("checking");
        account.setCreatedAt(LocalDateTime.now());
        account = accountRepository.save(account);

        transactionRepository.upsertTransaction(
            account.getId(), new java.math.BigDecimal("25.00"), "Coffee Shop",
            LocalDateTime.now(), LocalDateTime.now(), "plaid-txn-abc123");

        transactionRepository.upsertTransaction(
            account.getId(), new java.math.BigDecimal("25.00"), "Coffee Shop",
            LocalDateTime.now(), LocalDateTime.now(), "plaid-txn-abc123");

        var transactions = transactionRepository.findByAccountId(account.getId());
        assertThat(transactions).hasSize(1);
    }
}