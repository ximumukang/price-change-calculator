-- ========================================
-- 清理双重加密数据脚本
-- 问题：之前的代码在 Service 层手动加密 + MyBatis TypeHandler 再次加密，导致双重加密
-- 解决：删除所有现有数据，重新插入（仅开发环境）
-- 
-- 警告：此操作会删除所有 price_item 数据！
-- 生产环境请勿使用此脚本，应通过应用层逐步迁移
-- ========================================

USE price_change;

-- 查看当前数据（确认要删除）
SELECT id, name, LEFT(name, 50) as encrypted_name_preview FROM price_item;

-- 删除所有数据
DELETE FROM price_item;

-- 验证已清空
SELECT COUNT(*) as remaining_records FROM price_item;

SELECT '✓ 所有双重加密数据已清除，请重新插入数据' AS cleanup_status;
