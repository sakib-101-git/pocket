package com.pocket.pocket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);

    @Modifying
    @Query(value = "INSERT INTO transactions (account_id, amount, description, transaction_date, created_at, plaid_transaction_id) " +
                   "VALUES (:accountId, :amount, :description, :transactionDate, :createdAt, :plaidTransactionId) " +
                   "ON CONFLICT (plaid_transaction_id) DO NOTHING", nativeQuery = true)
    void upsertTransaction(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount,
                            @Param("description") String description, @Param("transactionDate") LocalDateTime transactionDate,
                            @Param("createdAt") LocalDateTime createdAt, @Param("plaidTransactionId") String plaidTransactionId);
}