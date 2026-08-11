package com.pocket.pocket;

import java.time.LocalDate;
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

import com.plaid.client.model.Transaction;

@SpringBootTest
@Testcontainers
class SyncWorkerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SyncWorker syncWorker;

    @Test
    void applyingSameBatchTwiceDoesNotDuplicate() {
        User user = new User();
        user.setEmail("worker-test@example.com");
        user.setPasswordHash("hashed");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        Account account = new Account();
        account.setUser(user);
        account.setName("Worker Test");
        account.setAccountType("checking");
        account.setCreatedAt(LocalDateTime.now());
        account = accountRepository.save(account);

        Transaction fakeTransaction = new Transaction()
            .transactionId("fake-plaid-txn-999")
            .amount(15.75)
            .name("Test Merchant")
            .date(LocalDate.now());

        syncWorker.applyTransactions(account, List.of(fakeTransaction));
        syncWorker.applyTransactions(account, List.of(fakeTransaction));

        var transactions = transactionRepository.findByAccountId(account.getId());
        assertThat(transactions).hasSize(1);
    }
}