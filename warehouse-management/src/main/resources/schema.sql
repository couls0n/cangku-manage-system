DROP TABLE IF EXISTS security_alert;
DROP TABLE IF EXISTS ebpf_event;
DROP TABLE IF EXISTS outbound_order_item;
DROP TABLE IF EXISTS outbound_order;
DROP TABLE IF EXISTS inbound_order_item;
DROP TABLE IF EXISTS inbound_order;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS supplier;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS product_category;
DROP TABLE IF EXISTS warehouse;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
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
  CONSTRAINT uk_username UNIQUE (username)
);

CREATE TABLE warehouse (
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

CREATE TABLE product_category (
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

CREATE TABLE product (
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
  CONSTRAINT uk_product_code UNIQUE (product_code)
);

CREATE TABLE stock (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity DECIMAL(10, 2) DEFAULT 0.00,
  frozen_quantity DECIMAL(10, 2) DEFAULT 0.00,
  batch_no VARCHAR(50),
  location VARCHAR(50),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0
);

CREATE TABLE supplier (
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

CREATE TABLE customer (
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

CREATE TABLE inbound_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  CONSTRAINT uk_inbound_order_no UNIQUE (order_no)
);

CREATE TABLE inbound_order_item (
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
  deleted INT DEFAULT 0
);

CREATE TABLE outbound_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
  CONSTRAINT uk_outbound_order_no UNIQUE (order_no)
);

CREATE TABLE outbound_order_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity DECIMAL(10, 2) DEFAULT 0.00,
  unit_price DECIMAL(10, 2) DEFAULT 0.00,
  total_price DECIMAL(10, 2) DEFAULT 0.00,
  remark VARCHAR(500),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0
);

CREATE TABLE ebpf_event (
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

CREATE TABLE security_alert (
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
