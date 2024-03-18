CREATE TABLE balance
(
    id               BIGSERIAL PRIMARY KEY,
    account_id       BIGINT         NOT NULL,
    currency         VARCHAR(5)     NOT NULL,
    available_amount NUMERIC(15, 2) NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (account_id, currency),
    FOREIGN KEY (account_id) REFERENCES account (id)
);
