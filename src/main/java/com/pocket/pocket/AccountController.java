package com.pocket.pocket;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RabbitTemplate rabbitTemplate;

    public AccountController(AccountService accountService, UserRepository userRepository,
                              AccountRepository accountRepository, RabbitTemplate rabbitTemplate) {
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.rabbitTemplate = rabbitTemplate;
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
    public ResponseEntity<Map<String, String>> syncTransactions(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Account not found");
        }

        account.setSyncStatus("PENDING");
        accountRepository.save(account);

        rabbitTemplate.convertAndSend(RabbitConfig.SYNC_QUEUE, new SyncJobMessage(account.getId()));

        return ResponseEntity.accepted().body(Map.of("status", "sync queued"));
    }

    @GetMapping("/accounts/{id}/sync/status")
    public Map<String, Object> getSyncStatus(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Account not found");
        }

        return Map.of(
                "status", account.getSyncStatus() != null ? account.getSyncStatus() : "NEVER_SYNCED",
                "lastSyncedAt", account.getLastSyncedAt() != null ? account.getLastSyncedAt().toString() : ""
        );
    }
}