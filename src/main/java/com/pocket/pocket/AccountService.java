package com.pocket.pocket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public List<AccountResponse> getAccountsForUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        return accountRepository.findByUserId(user.getId()).stream()
            .map(account -> new AccountResponse(account.getId(), account.getName(), account.getAccountType()))
            .collect(Collectors.toList());
    }

    public AccountResponse createAccount(CreateAccountRequest request, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        Account account = new Account();
        account.setUser(user);
        account.setName(request.getName());
        account.setAccountType(request.getAccountType());
        account.setCreatedAt(LocalDateTime.now());

        Account saved = accountRepository.save(account);
        return new AccountResponse(saved.getId(), saved.getName(), saved.getAccountType());
    }
}