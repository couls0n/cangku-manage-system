ALTER TABLE stock_adjustment ADD COLUMN IF NOT EXISTS status VARCHAR(16) DEFAULT 'APPROVED' NOT NULL;
ALTER TABLE stock_adjustment ADD COLUMN IF NOT EXISTS approver_id BIGINT;
ALTER TABLE stock_adjustment ADD COLUMN IF NOT EXISTS approved_time TIMESTAMP;
ALTER TABLE stock_adjustment ADD COLUMN IF NOT EXISTS approval_comment VARCHAR(500);
CREATE INDEX IF NOT EXISTS idx_stock_adjustment_status_time ON stock_adjustment (status, create_time);

ALTER TABLE stock_check_record ADD COLUMN IF NOT EXISTS status VARCHAR(16) DEFAULT 'APPROVED' NOT NULL;
ALTER TABLE stock_check_record ADD COLUMN IF NOT EXISTS approver_id BIGINT;
ALTER TABLE stock_check_record ADD COLUMN IF NOT EXISTS approved_time TIMESTAMP;
ALTER TABLE stock_check_record ADD COLUMN IF NOT EXISTS approval_comment VARCHAR(500);
CREATE INDEX IF NOT EXISTS idx_stock_check_status_time ON stock_check_record (status, create_time);
