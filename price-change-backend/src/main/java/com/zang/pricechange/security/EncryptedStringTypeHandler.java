package com.zang.pricechange.security;

import com.zang.pricechange.common.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis-Plus 加密字符串类型处理器
 * 自动对写入数据库的字符串进行 AES 加密，读取时自动解密
 *
 * 使用方式（仅在需要加密的字段上显式指定）：
 * @TableField(typeHandler = EncryptedStringTypeHandler.class)
 * private String sensitiveField;
 *
 * 注意：不是 Spring Bean，通过 SpringContextUtil 获取 AesEncryptor
 */
@Slf4j
public class EncryptedStringTypeHandler extends BaseTypeHandler<String> {

    private static volatile AesEncryptor aesEncryptor;

    /**
     * 获取 AesEncryptor 实例（懒加载，双重检查锁定保证线程安全）
     */
    private AesEncryptor getEncryptor() {
        if (aesEncryptor == null) {
            synchronized (EncryptedStringTypeHandler.class) {
                if (aesEncryptor == null) {
                    aesEncryptor = SpringContextUtil.getBean(AesEncryptor.class);
                    log.info("EncryptedStringTypeHandler 已获取 AesEncryptor Bean");
                }
            }
        }
        return aesEncryptor;
    }

    /**
     * 设置参数：加密后存入数据库
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        String encrypted = getEncryptor().encrypt(parameter);
        ps.setString(i, encrypted);
    }

    /**
     * 获取结果：从数据库读取并解密
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String encrypted = rs.getString(columnName);
        return decryptOrNull(encrypted);
    }

    /**
     * 获取结果：从数据库读取并解密（按列索引）
     */
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String encrypted = rs.getString(columnIndex);
        return decryptOrNull(encrypted);
    }

    /**
     * 获取结果：从存储过程返回值并解密
     */
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String encrypted = cs.getString(columnIndex);
        return decryptOrNull(encrypted);
    }

    /**
     * 辅助方法：解密密文，处理 null 情况
     */
    private String decryptOrNull(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return encrypted;
        }
        try {
            return getEncryptor().decrypt(encrypted);
        } catch (Exception e) {
            log.warn("解密失败，返回原始值: {}", e.getMessage());
            return encrypted;
        }
    }
}
