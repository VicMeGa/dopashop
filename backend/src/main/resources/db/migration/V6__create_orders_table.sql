CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    address_id BIGINT NOT NULL REFERENCES address(id),
    payment_method_id BIGINT NOT NULL REFERENCES payment_method(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
