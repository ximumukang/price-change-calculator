# 数据库迁移指南

## 问题说明

`price_item` 表的 `name` 字段原始定义为 `VARCHAR(100)`，但由于使用了 AES-256-GCM 加密存储，加密后的数据经过 Base64 编码会导致长度显著增加。

**长度计算：**
- 原始数据：最多 100 字符
- 加密后：IV(12字节) + 密文(~100字节) + GCM Tag(16字节) ≈ 128 字节
- Base64 编码后：128 * 1.33 ≈ 170 字符

因此需要将字段长度扩大到 `VARCHAR(500)` 以容纳加密数据。

## 迁移步骤

### 方法一：使用 MySQL 命令行工具

```bash
# Windows PowerShell
mysql -u root -p price_change < src/main/resources/db/migration/V1__alter_name_column_length.sql

# 或直接执行 SQL
mysql -u root -p price_change -e "ALTER TABLE price_item MODIFY COLUMN name VARCHAR(500) NOT NULL;"
```

### 方法二：使用数据库管理工具

1. 打开你喜欢的数据库管理工具（如 Navicat、DBeaver、MySQL Workbench）
2. 连接到 `price_change` 数据库
3. 执行以下 SQL 语句：

```sql
ALTER TABLE price_item MODIFY COLUMN name VARCHAR(500) NOT NULL;
```

### 方法三：重新创建数据库（仅开发环境）

如果是开发环境且可以清空数据，可以删除并重新创建数据库：

```sql
DROP DATABASE IF EXISTS price_change;
CREATE DATABASE price_change CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后重新运行应用，让 MyBatis-Plus 根据更新后的 `schema.sql` 自动创建表结构。

## 验证迁移

执行以下 SQL 验证字段是否已成功修改：

```sql
DESCRIBE price_item;
```

应该看到 `name` 字段的类型为 `varchar(500)`。

## 注意事项

- 此迁移不会丢失现有数据
- 如果表中已有加密数据，确保在迁移前备份数据库
- 迁移后可以正常插入和读取加密的 name 字段
