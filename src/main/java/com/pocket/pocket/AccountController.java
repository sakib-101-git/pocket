package com.pocket.pocket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.plaid.client.model.TransactionsSyncRequest;
import com.plaid.client.model.TransactionsSyncResponse;
import com.plaid.client.request.PlaidApi;

import jakarta.validation.Valid;
import retrofit2.Response;

@RestController
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PlaidApi plaidApi;

    public AccountController(AccountService accountService, UserRepository userRepository,
                              AccountRepository accountRepository, TransactionRepository transactionRepository,
                              PlaidApi plaidApi) {
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.plaidApi = plaidApi;
    }

    @GetMapping("/accounts")
    public List<AccountResponse> getAccounts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.getAccountsForUser(email);
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AccountResponse response = accountService.createAccount(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/accounts/{id}/sync")
    @Transactional
    public Map<String, Object> syncTransactions(@PathVariable Long id) throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Account not found");
        }

        TransactionsSyncRequest request = new TransactionsSyncRequest()
                .accessToken(account.getPlaidAccessToken());

        Response<TransactionsSyncResponse> response = plaidApi.transactionsSync(request).execute();

        if (!response.isSuccessful()) {
            String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            throw new RuntimeException("Plaid sync failed: " + errorMessage);
        }

        List<com.plaid.client.model.Transaction> added = response.body().getAdded();

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

        return Map.of("syncedCount", added.size());
    }
}