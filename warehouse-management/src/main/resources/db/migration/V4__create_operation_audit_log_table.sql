CREATE TABLE IF NOT EXISTS operation_audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id VARCHAR(64),
  operator_id BIGINT,
  username VARCHAR(64),
  action VARCHAR(64) NOT NULL,
  resource VARCHAR(64) NOT NULL,
  http_method VARCHAR(16),
  request_uri VARCHAR(255),
  ip_address VARCHAR(64),
  success INT DEFAULT 1,
  result_code INT,
  error_message VARCHAR(500),
  request_payload VARCHAR(2000),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  CONSTRAINT fk_audit_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
);

CREATE INDEX IF NOT EXISTS idx_audit_request_id ON operation_audit_log (request_id);
CREATE INDEX IF NOT EXISTS idx_audit_operator_time ON operation_audit_log (operator_id, create_time);
CREATE INDEX IF NOT EXISTS idx_audit_action_time ON operation_audit_log (action, create_time);
