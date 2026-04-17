-- ========================================
-- 数据库修复脚本
-- 问题：name 字段长度不足导致加密数据无法存储
-- 解决：将 VARCHAR(100) 扩展为 VARCHAR(500)
-- ========================================

USE price_change;

-- 修改 name 字段长度
ALTER TABLE price_item 
MODIFY COLUMN name VARCHAR(500) NOT NULL 
COMMENT '价格项名称（AES-256-GCM加密存储）';

-- 验证修改结果
DESCRIBE price_item;

-- 完成提示
SELECT '✓ name 字段已成功修改为 VARCHAR(500)' AS migration_status;
