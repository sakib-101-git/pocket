package com.pocket.pocket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                               AccountRepository accountRepository,
                               UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Page<TransactionResponse> getTransactionsForAccount(Long accountId, String email, Pageable pageable) {
        User user = userRepository.findByEmail(email).orElseThrow();

        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Account not found");
        }

        return transactionRepository.findByAccountId(accountId, pageable)
            .map(t -> new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getDescription(),
                t.getTransactionDate(),
                t.getCategory() != null ? t.getCategory().getName() : null
            ));
    }
}