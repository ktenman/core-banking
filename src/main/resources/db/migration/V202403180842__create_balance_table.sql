CREATE TABLE balance
(
    id               BIGSERIAL PRIMARY KEY,
    account_id       BIGINT         NOT NULL,
    currency         VARCHAR(5)     NOT NULL,
    available_amount NUMERIC(20, 8) NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (account_id, currency),
    FOREIGN KEY (account_id) REFERENCES account (id)
);
