INSERT INTO warehouse (id, warehouse_code, warehouse_name, address, manager, phone, status, remark, deleted)
VALUES
  (1, 'WH001', 'Main Warehouse', 'Beijing Chaoyang District', 'Zhang San', '010-12345678', 1, 'Primary warehouse', 0),
  (2, 'WH002', 'East Warehouse', 'Shanghai Pudong District', 'Li Si', '021-87654321', 1, 'Regional warehouse', 0);

INSERT INTO sys_user (id, username, password, real_name, phone, email, status, role, warehouse_id, deleted)
VALUES
  (1, 'admin', '$2a$10$E0UdvZysvTuJaCTI3sukhuib3cjx6S8iI720lsmEGLSGvhaq.KweG', 'System Admin', '13800138000', 'admin@example.com', 1, 2, 1, 0),
  (2, 'operator', '$2a$10$E0UdvZysvTuJaCTI3sukhuib3cjx6S8iI720lsmEGLSGvhaq.KweG', 'Warehouse Operator', '13800138001', 'operator@example.com', 1, 1, 2, 0);

INSERT INTO product_category (id, category_name, parent_id, sort, status, remark, deleted)
VALUES
  (1, 'Electronics', 0, 1, 1, 'Electronic devices', 0),
  (2, 'Office Supplies', 0, 2, 1, 'Office products', 0),
  (3, 'Daily Necessities', 0, 3, 1, 'Consumables', 0);

INSERT INTO product (id, product_code, product_name, category_id, unit, price, specification, brand, status, remark, deleted)
VALUES
  (1, 'P001', 'Laptop', 1, 'unit', 5999.00, '15.6-inch', 'Lenovo', 1, 'Standard laptop', 0),
  (2, 'P002', 'Desktop PC', 1, 'unit', 3999.00, 'Intel i5', 'Dell', 1, 'Office desktop', 0),
  (3, 'P003', 'Printer', 2, 'unit', 1299.00, 'A4', 'HP', 1, 'Laser printer', 0);

INSERT INTO supplier (id, supplier_code, supplier_name, contact_person, phone, address, email, status, remark, deleted)
VALUES
  (1, 'S001', 'Beijing Tech Ltd', 'Wang Jingli', '13900139000', 'Beijing Haidian District', 'supplier1@example.com', 1, 'Primary supplier', 0),
  (2, 'S002', 'Shanghai Trade Ltd', 'Zhao Jingli', '13900139001', 'Shanghai Huangpu District', 'supplier2@example.com', 1, 'Regional supplier', 0);

INSERT INTO customer (id, customer_code, customer_name, contact_person, phone, address, email, status, remark, deleted)
VALUES
  (1, 'C001', 'Guangzhou Sales Co', 'Chen Jingli', '13700137000', 'Guangzhou Tianhe District', 'customer1@example.com', 1, 'VIP customer', 0),
  (2, 'C002', 'Shenzhen Tech Co', 'Liu Jingli', '13700137001', 'Shenzhen Nanshan District', 'customer2@example.com', 1, 'Enterprise customer', 0);

INSERT INTO stock (id, warehouse_id, product_id, quantity, frozen_quantity, batch_no, location, deleted)
VALUES
  (1, 1, 1, 20.00, 0.00, 'B20260301', 'A-01-01', 0),
  (2, 1, 2, 15.00, 1.00, 'B20260302', 'A-01-02', 0),
  (3, 2, 3, 30.00, 0.00, 'B20260303', 'B-02-01', 0);
