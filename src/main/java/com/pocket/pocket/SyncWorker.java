package com.pocket.pocket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.plaid.client.model.TransactionsSyncRequest;
import com.plaid.client.model.TransactionsSyncResponse;
import com.plaid.client.request.PlaidApi;

import retrofit2.Response;

@Component
public class SyncWorker {

    private final PlaidApi plaidApi;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public SyncWorker(PlaidApi plaidApi, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.plaidApi = plaidApi;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @RabbitListener(queues = RabbitConfig.SYNC_QUEUE)
    @Transactional
    public void handleSyncJob(SyncJobMessage message) throws Exception {
        Account account = accountRepository.findById(message.getAccountId())
            .orElseThrow(() -> new RuntimeException("Account not found: " + message.getAccountId()));

        TransactionsSyncRequest request = new TransactionsSyncRequest()
            .accessToken(account.getPlaidAccessToken());

        Response<TransactionsSyncResponse> response = plaidApi.transactionsSync(request).execute();

        if (!response.isSuccessful()) {
            String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            throw new RuntimeException("Plaid sync failed: " + errorMessage);
        }

        applyTransactions(account, response.body().getAdded());
    }

    @Transactional
    public void applyTransactions(Account account, List<com.plaid.client.model.Transaction> added) {
        for (com.plaid.client.model.Transaction t : added) {
            transactionRepository.upsertTransaction(
                account.getId(),
                t.getAmount() != null ? BigDecimal.valueOf(t.getAmount()) : BigDecimal.ZERO,
                t.getName(),
                t.getDate() != null ? t.getDate().atStartOfDay() : LocalDateTime.now(),
                LocalDateTime.now(),
                t.getTransactionId()
            );
        }
        System.out.println("Synced " + added.size() + " transactions for account " + account.getId());
    }
}