package com.pocket.pocket;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
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
}
