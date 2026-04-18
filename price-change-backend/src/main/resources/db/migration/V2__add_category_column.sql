-- =============================================
-- V2: 独立分类表
-- 1. 创建 category 表
-- 2. 给 price_item 添加 category_id 外键列
-- 3. 添加外键约束和索引
-- =============================================

-- 1. 创建 category 表
CREATE TABLE IF NOT EXISTS category (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  sort_order INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_category_user_name (user_id, name),
  INDEX idx_category_user_id (user_id)
);

-- 2. 给 price_item 添加 category_id 列
ALTER TABLE price_item ADD COLUMN category_id BIGINT DEFAULT NULL COMMENT '分类ID（外键关联category表）' AFTER user_id;

-- 3. 添加外键约束
ALTER TABLE price_item ADD CONSTRAINT fk_price_item_category
  FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL;

-- 4. 添加索引
CREATE INDEX idx_price_item_category_id ON price_item(user_id, category_id);
