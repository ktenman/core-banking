CREATE TABLE account
(
    id           BIGSERIAL PRIMARY KEY,
    reference VARCHAR(36) NOT NULL UNIQUE,
    country_code VARCHAR(3)  NOT NULL
        CONSTRAINT country_code_three_letters CHECK (LENGTH(country_code) = 3),
    customer_id  BIGINT      NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
