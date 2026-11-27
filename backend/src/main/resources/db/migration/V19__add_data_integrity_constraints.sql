ALTER TABLE product ADD CONSTRAINT ck_product_stock_non_negative CHECK (stock >= 0);
ALTER TABLE product ADD CONSTRAINT ck_product_price_positive CHECK (price > 0);
ALTER TABLE order_item ADD CONSTRAINT ck_order_item_quantity_positive CHECK (quantity > 0);
ALTER TABLE orders ADD CONSTRAINT ck_orders_total_non_negative CHECK (total >= 0);
