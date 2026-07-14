ALTER TABLE payment_transaction DROP CONSTRAINT payment_transaction_order_id_key;
CREATE INDEX idx_payment_transaction_order_id ON payment_transaction(order_id);
ALTER TABLE payment_transaction ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();
