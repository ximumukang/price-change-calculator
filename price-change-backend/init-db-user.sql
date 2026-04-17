-- ============================================
-- 数据库用户初始化脚本
-- 用途：创建专用应用用户，实施最小权限原则
-- 执行方式：mysql -u root -p < init-db-user.sql
-- ============================================

-- 1. 创建专用应用用户
-- 注意：生产环境请替换 'ChangeMe_StrongPassword_2026' 为强密码
CREATE USER IF NOT EXISTS 'price_app'@'localhost' 
IDENTIFIED BY 'ChangeMe_StrongPassword_2026';

-- 2. 授予最小必要权限（仅 CRUD 操作）
GRANT SELECT, INSERT, UPDATE, DELETE ON price_change.* TO 'price_app'@'localhost';

-- 3. 刷新权限使配置生效
FLUSH PRIVILEGES;

-- 4. 验证用户权限
SHOW GRANTS FOR 'price_app'@'localhost';

-- ============================================
-- 说明：
-- 1. 此用户仅有数据操作权限，无 DDL 权限
-- 2. 禁止 CREATE/DROP/ALTER 等结构变更操作
-- 3. 仅允许从 localhost 连接，限制网络访问
-- 4. 生产环境必须修改默认密码
-- ============================================

-- 如需撤销权限（紧急情况下使用）：
-- REVOKE ALL PRIVILEGES ON price_change.* FROM 'price_app'@'localhost';
-- FLUSH PRIVILEGES;

-- 如需删除用户：
-- DROP USER 'price_app'@'localhost';
