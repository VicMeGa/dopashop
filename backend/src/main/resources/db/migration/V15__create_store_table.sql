CREATE TABLE store (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
