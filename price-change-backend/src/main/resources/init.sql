-- 创建数据库
CREATE DATABASE IF NOT EXISTS price_change DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE price_change;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建价格项表
CREATE TABLE IF NOT EXISTS price_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  current_value DECIMAL(18,4) NOT NULL,
  target_value DECIMAL(18,4) NOT NULL,
  change_percent DECIMAL(10,4),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_price_item_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建索引
CREATE INDEX idx_price_item_user_id ON price_item(user_id);
