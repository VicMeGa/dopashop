-- Un usuario solo puede reseñar cada producto una vez.
-- Si ya existen duplicados, fallará; limpiar previamente con:
--   DELETE FROM review r USING review r2
--   WHERE r.id > r2.id AND r.product_id = r2.product_id AND r.user_id = r2.user_id;
ALTER TABLE review
    ADD CONSTRAINT uk_review_product_user UNIQUE (product_id, user_id);