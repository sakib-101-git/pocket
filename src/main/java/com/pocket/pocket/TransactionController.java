package com.pocket.pocket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/accounts/{id}/transactions")
    public Page<TransactionResponse> getTransactions(@PathVariable Long id, Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return transactionService.getTransactionsForAccount(id, email, pageable);
    }
}