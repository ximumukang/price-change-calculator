// Service 所在包
package com.zang.pricechange.service;

// MyBatis-Plus 查询条件构造器
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 输入清理工具类
import com.zang.pricechange.common.InputSanitizer;
// 价格项实体类
import com.zang.pricechange.entity.PriceItem;
// 价格项 Mapper 接口
import com.zang.pricechange.mapper.PriceItemMapper;
// Spring 注解
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 价格项服务类
 * 处理涨跌幅记录的业务逻辑：创建、查询、删除
 * 涨跌幅计算公式：(目标值 - 当前值) / 当前值 * 100%
 */
@Service
@RequiredArgsConstructor
public class PriceItemService {

    private final PriceItemMapper priceItemMapper;

    /**
     * 创建涨跌幅记录
     * @param userId 当前用户 ID
     * @param name 名称
     * @param currentValue 当前值（必须大于0，用于计算涨跌幅的分母）
     * @param targetValue 目标值（可以为负数，表示预期下跌）
     * @return 创建好的记录
     */
    public PriceItem create(Long userId, String name, BigDecimal currentValue, BigDecimal targetValue) {
        // 参数校验：当前值必须大于0（防止除零错误）
        if (currentValue == null || currentValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("当前值必须大于0");
        }
        
        // 参数校验：名称不能为空
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }

        // 对名称进行 HTML 转义（加密由 EncryptedStringTypeHandler 自动处理）
        String sanitizedName = InputSanitizer.sanitizeForHtml(name);

        PriceItem item = new PriceItem();
        item.setUserId(userId);
        item.setName(sanitizedName);
        item.setCurrentValue(currentValue);
        item.setTargetValue(targetValue);
        
        // 计算涨跌幅：(目标值 - 当前值) / 当前值 * 100%
        BigDecimal changePercent = targetValue.subtract(currentValue)
                .divide(currentValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        item.setChangePercent(changePercent);
        
        priceItemMapper.insert(item);
        return item;
    }

    /**
     * 查询当前用户的所有涨跌幅记录
     * @param userId 用户 ID
     * @param sortOrder 排序方式：asc / desc
     * @return 记录列表
     */
    public List<PriceItem> list(Long userId, String sortOrder) {
        LambdaQueryWrapper<PriceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceItem::getUserId, userId);

        if ("asc".equalsIgnoreCase(sortOrder)) {
            wrapper.orderByAsc(PriceItem::getChangePercent);
        } else if ("desc".equalsIgnoreCase(sortOrder)) {
            wrapper.orderByDesc(PriceItem::getChangePercent);
        } else {
            wrapper.orderByDesc(PriceItem::getCreatedAt);
        }

        return priceItemMapper.selectList(wrapper);
    }

    /**
     * 删除涨跌幅记录
     * @param userId 当前用户 ID
     * @param id 要删除的记录 ID
     */
    public void delete(Long userId, Long id) {
        LambdaQueryWrapper<PriceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceItem::getUserId, userId).eq(PriceItem::getId, id);
        int deleted = priceItemMapper.delete(wrapper);
        
        if (deleted == 0) {
            throw new IllegalArgumentException("记录不存在或无权删除");
        }
    }

    /**
     * 更新涨跌幅记录
     * @param userId 当前用户 ID
     * @param id 要更新的记录 ID
     * @param name 新名称
     * @param currentValue 新当前值（必须大于0）
     * @param targetValue 新目标值
     * @return 更新后的记录
     */
    public PriceItem update(Long userId, Long id, String name, BigDecimal currentValue, BigDecimal targetValue) {
        // 参数校验：当前值必须大于0（防止除零错误）
        if (currentValue == null || currentValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("当前值必须大于0");
        }
        
        // 参数校验：名称不能为空
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }

        LambdaQueryWrapper<PriceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceItem::getUserId, userId).eq(PriceItem::getId, id);
        PriceItem existing = priceItemMapper.selectOne(wrapper);
        if (existing == null) {
            throw new IllegalArgumentException("记录不存在或无权修改");
        }

        // 对名称进行 HTML 转义（加密由 EncryptedStringTypeHandler 自动处理）
        String sanitizedName = InputSanitizer.sanitizeForHtml(name);
        existing.setName(sanitizedName);
        existing.setCurrentValue(currentValue);
        existing.setTargetValue(targetValue);

        // 重新计算涨跌幅
        BigDecimal changePercent = targetValue.subtract(currentValue)
                .divide(currentValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        existing.setChangePercent(changePercent);

        priceItemMapper.updateById(existing);
        return existing;
    }
}
