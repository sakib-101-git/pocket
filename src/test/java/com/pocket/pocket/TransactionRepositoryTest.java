package com.pocket.pocket;

import java.math.BigDecimal;
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
class TransactionRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void savesTransactionWithNoCategory() {
        User user = new User();
        user.setEmail("txn-owner@example.com");
        user.setPasswordHash("hashed");
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setUser(savedUser);
        account.setName("Checking");
        account.setAccountType("checking");
        account.setCreatedAt(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(savedAccount);
        transaction.setAmount(new BigDecimal("42.50"));
        transaction.setDescription("Coffee shop");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setCreatedAt(LocalDateTime.now());
        // deliberately no category set

        transactionRepository.save(transaction);

        List<Transaction> transactions = transactionRepository.findByAccountId(savedAccount.getId());

        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getCategory()).isNull();
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo("42.50");
    }
}