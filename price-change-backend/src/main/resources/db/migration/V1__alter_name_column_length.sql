-- 修改 price_item 表的 name 字段长度
-- 原因：name 字段使用 AES-256-GCM 加密存储，加密后的数据经过 Base64 编码会显著增加长度
-- 原始 100 字符 -> 加密后约 170+ 字符
ALTER TABLE price_item MODIFY COLUMN name VARCHAR(500) NOT NULL;
