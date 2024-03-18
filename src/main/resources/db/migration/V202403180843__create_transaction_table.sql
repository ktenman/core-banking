CREATE TABLE transaction
(
    id                        BIGSERIAL PRIMARY KEY,
    account_id                BIGINT         NOT NULL,
    balance_id                BIGINT         NOT NULL,
    amount                    NUMERIC(15, 2) NOT NULL,
    direction                 VARCHAR(3)     NOT NULL,
    description               VARCHAR(255)   NOT NULL,
    balance_after_transaction NUMERIC(15, 2) NOT NULL,
    created_at                TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account (id),
    FOREIGN KEY (balance_id) REFERENCES balance (id),
    CHECK (direction IN ('IN', 'OUT'))
);
