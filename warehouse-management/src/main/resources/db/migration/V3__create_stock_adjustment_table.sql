CREATE TABLE IF NOT EXISTS stock_adjustment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id VARCHAR(64) NOT NULL,
  stock_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  operator_id BIGINT NOT NULL,
  adjustment_type VARCHAR(16) NOT NULL,
  quantity_change DECIMAL(10, 2) NOT NULL,
  before_quantity DECIMAL(10, 2) NOT NULL,
  after_quantity DECIMAL(10, 2) NOT NULL,
  batch_no VARCHAR(50),
  reason VARCHAR(128) NOT NULL,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT uk_stock_adjustment_request_id UNIQUE (request_id),
  CONSTRAINT fk_adjustment_stock FOREIGN KEY (stock_id) REFERENCES stock (id),
  CONSTRAINT fk_adjustment_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
  CONSTRAINT fk_adjustment_product FOREIGN KEY (product_id) REFERENCES product (id),
  CONSTRAINT fk_adjustment_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
);

CREATE INDEX IF NOT EXISTS idx_adjustment_warehouse_product_time ON stock_adjustment (warehouse_id, product_id, create_time);
CREATE INDEX IF NOT EXISTS idx_adjustment_stock_time ON stock_adjustment (stock_id, create_time);
