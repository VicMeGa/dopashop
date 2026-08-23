-- a. Crear usuario "sistema" si no existe
INSERT INTO users (email, password_hash, full_name, role, created_at)
SELECT 'system@dopapc.internal', '$2a$10$NotUsablePasswordHashRandomValueForSystemUserOnly1234567890', 'dopaPC', 'ADMIN', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'system@dopapc.internal'
);

-- b. Insertar la tienda oficial si no existe
INSERT INTO store (name, description, owner_id, created_at)
SELECT 'dopaPC Store', 'Catálogo oficial de dopaPC', id, NOW()
FROM users
WHERE email = 'system@dopapc.internal'
  AND NOT EXISTS (
      SELECT 1 FROM store WHERE name = 'dopaPC Store'
  );

-- c. Agregar la columna store_id a product, poblar y hacer NOT NULL con FK
ALTER TABLE product ADD COLUMN store_id BIGINT;

UPDATE product 
SET store_id = (SELECT id FROM store WHERE name = 'dopaPC Store') 
WHERE store_id IS NULL;

ALTER TABLE product ALTER COLUMN store_id SET NOT NULL;

ALTER TABLE product ADD CONSTRAINT fk_product_store FOREIGN KEY (store_id) REFERENCES store(id);
