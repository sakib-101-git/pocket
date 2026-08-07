package com.pocket.pocket;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
