CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS category (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  sort_order INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_category_user_name (user_id, name),
  INDEX idx_category_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS price_item (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  category_id BIGINT DEFAULT NULL COMMENT '分类ID（外键关联category表）',
  name VARCHAR(500) NOT NULL COMMENT '价格项名称（AES-256-GCM加密存储）',
  current_value DECIMAL(18,4) NOT NULL,
  target_value DECIMAL(18,4) NOT NULL,
  change_percent DECIMAL(10,4),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_price_item_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL
);

CREATE INDEX idx_price_item_user_id ON price_item(user_id);
CREATE INDEX idx_price_item_category_id ON price_item(user_id, category_id);
