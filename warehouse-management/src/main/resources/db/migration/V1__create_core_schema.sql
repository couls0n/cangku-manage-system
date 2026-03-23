CREATE TABLE IF NOT EXISTS warehouse (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  warehouse_code VARCHAR(50) NOT NULL,
  warehouse_name VARCHAR(100) NOT NULL,
  address VARCHAR(200),
  manager VARCHAR(50),
  phone VARCHAR(20),
  status INT DEFAULT 1,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT uk_warehouse_code UNIQUE (warehouse_code)
);

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50),
  phone VARCHAR(20),
  email VARCHAR(100),
  status INT DEFAULT 1,
  role INT DEFAULT 1,
  warehouse_id BIGINT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT uk_username UNIQUE (username),
  CONSTRAINT fk_user_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id)
);

CREATE TABLE IF NOT EXISTS product_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_name VARCHAR(50) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  sort INT DEFAULT 0,
  status INT DEFAULT 1,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS product (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_code VARCHAR(50) NOT NULL,
  product_name VARCHAR(100) NOT NULL,
  category_id BIGINT,
  unit VARCHAR(20),
  price DECIMAL(10, 2) DEFAULT 0.00,
  specification VARCHAR(100),
  brand VARCHAR(50),
  status INT DEFAULT 1,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT uk_product_code UNIQUE (product_code),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category (id)
);

CREATE TABLE IF NOT EXISTS stock (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity DECIMAL(10, 2) DEFAULT 0.00,
  frozen_quantity DECIMAL(10, 2) DEFAULT 0.00,
  batch_no VARCHAR(50),
  location VARCHAR(50),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT fk_stock_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
  CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE IF NOT EXISTS supplier (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplier_code VARCHAR(50) NOT NULL,
  supplier_name VARCHAR(100) NOT NULL,
  contact_person VARCHAR(50),
  phone VARCHAR(20),
  address VARCHAR(200),
  email VARCHAR(100),
  status INT DEFAULT 1,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT uk_supplier_code UNIQUE (supplier_code)
);

CREATE TABLE IF NOT EXISTS customer (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_code VARCHAR(50) NOT NULL,
  customer_name VARCHAR(100) NOT NULL,
  contact_person VARCHAR(50),
  phone VARCHAR(20),
  address VARCHAR(200),
  email VARCHAR(100),
  status INT DEFAULT 1,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT uk_customer_code UNIQUE (customer_code)
);

CREATE TABLE IF NOT EXISTS inbound_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id VARCHAR(64) NOT NULL,
  order_no VARCHAR(50) NOT NULL,
  warehouse_id BIGINT NOT NULL,
  supplier_id BIGINT,
  operator_id BIGINT,
  order_time TIMESTAMP,
  total_amount DECIMAL(10, 2) DEFAULT 0.00,
  status INT DEFAULT 0,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT uk_inbound_request_id UNIQUE (request_id),
  CONSTRAINT uk_inbound_order_no UNIQUE (order_no),
  CONSTRAINT fk_inbound_order_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
  CONSTRAINT fk_inbound_order_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id),
  CONSTRAINT fk_inbound_order_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS inbound_order_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity DECIMAL(10, 2) DEFAULT 0.00,
  unit_price DECIMAL(10, 2) DEFAULT 0.00,
  total_price DECIMAL(10, 2) DEFAULT 0.00,
  batch_no VARCHAR(50),
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT fk_inbound_item_order FOREIGN KEY (order_id) REFERENCES inbound_order (id),
  CONSTRAINT fk_inbound_item_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE IF NOT EXISTS outbound_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id VARCHAR(64) NOT NULL,
  order_no VARCHAR(50) NOT NULL,
  warehouse_id BIGINT NOT NULL,
  customer_id BIGINT,
  operator_id BIGINT,
  order_time TIMESTAMP,
  total_amount DECIMAL(10, 2) DEFAULT 0.00,
  status INT DEFAULT 0,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT uk_outbound_request_id UNIQUE (request_id),
  CONSTRAINT uk_outbound_order_no UNIQUE (order_no),
  CONSTRAINT fk_outbound_order_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
  CONSTRAINT fk_outbound_order_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
  CONSTRAINT fk_outbound_order_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
);

CREATE TABLE IF NOT EXISTS outbound_order_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity DECIMAL(10, 2) DEFAULT 0.00,
  unit_price DECIMAL(10, 2) DEFAULT 0.00,
  total_price DECIMAL(10, 2) DEFAULT 0.00,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT fk_outbound_item_order FOREIGN KEY (order_id) REFERENCES outbound_order (id),
  CONSTRAINT fk_outbound_item_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE IF NOT EXISTS ebpf_event (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_type VARCHAR(32) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  process_id BIGINT,
  process_name VARCHAR(128),
  syscall_name VARCHAR(64),
  target_path VARCHAR(255),
  remote_address VARCHAR(128),
  remote_port INT,
  protocol VARCHAR(16),
  summary VARCHAR(255) NOT NULL,
  detail VARCHAR(1000),
  warehouse_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS security_alert (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  alert_type VARCHAR(64) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  title VARCHAR(255) NOT NULL,
  content VARCHAR(1000),
  first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  hit_count INT DEFAULT 1,
  status VARCHAR(16) DEFAULT 'OPEN'
);

CREATE INDEX IF NOT EXISTS idx_user_warehouse_deleted ON sys_user (warehouse_id, deleted);
CREATE INDEX IF NOT EXISTS idx_product_category_deleted ON product (category_id, deleted);
CREATE INDEX IF NOT EXISTS idx_stock_warehouse_product_deleted ON stock (warehouse_id, product_id, deleted);
CREATE INDEX IF NOT EXISTS idx_stock_warehouse_product_batch ON stock (warehouse_id, product_id, batch_no);
CREATE INDEX IF NOT EXISTS idx_inbound_order_warehouse_status ON inbound_order (warehouse_id, status, order_time);
CREATE INDEX IF NOT EXISTS idx_inbound_item_order_deleted ON inbound_order_item (order_id, deleted);
CREATE INDEX IF NOT EXISTS idx_outbound_order_warehouse_status ON outbound_order (warehouse_id, status, order_time);
CREATE INDEX IF NOT EXISTS idx_outbound_item_order_deleted ON outbound_order_item (order_id, deleted);
CREATE INDEX IF NOT EXISTS idx_event_created_at ON ebpf_event (created_at);
CREATE INDEX IF NOT EXISTS idx_event_type_severity ON ebpf_event (event_type, severity);
CREATE INDEX IF NOT EXISTS idx_alert_status_last_seen ON security_alert (status, last_seen);
