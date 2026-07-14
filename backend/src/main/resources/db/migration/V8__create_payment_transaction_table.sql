CREATE TABLE payment_transaction (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    amount NUMERIC(10,2) NOT NULL,
    processed_at TIMESTAMP
);
