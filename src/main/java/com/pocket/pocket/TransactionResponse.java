package com.pocket.pocket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private final Long id;
    private final BigDecimal amount;
    private final String description;
    private final LocalDateTime transactionDate;
    private final String category;

    public TransactionResponse(Long id, BigDecimal amount, String description,
                                LocalDateTime transactionDate, String category) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.category = category;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getCategory() { return category; }

}