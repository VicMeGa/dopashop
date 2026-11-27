CREATE TABLE payment_method (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    alias VARCHAR(255) NOT NULL,
    card_last_four VARCHAR(4) NOT NULL,
    card_brand VARCHAR(255) NOT NULL
);
