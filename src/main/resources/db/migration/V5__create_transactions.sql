CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    category_id BIGINT REFERENCES categories(id),
    amount NUMERIC(12, 2) NOT NULL,
    description VARCHAR(255),
    transaction_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);