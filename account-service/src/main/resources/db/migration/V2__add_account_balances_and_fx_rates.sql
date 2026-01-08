-- Flyway migration: add account_balances and fx_rates tables

CREATE TABLE IF NOT EXISTS account_balances (
    account_id BIGINT NOT NULL,
    currency CHAR(3) NOT NULL,
    balance DECIMAL(19,4) NOT NULL DEFAULT 0,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, currency)
);

CREATE TABLE IF NOT EXISTS fx_rates (
    id BIGSERIAL PRIMARY KEY,
    base_currency CHAR(3) NOT NULL,
    target_currency CHAR(3) NOT NULL,
    rate DECIMAL(19,8) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(100),
    UNIQUE (base_currency, target_currency)
);

