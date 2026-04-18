package com.zang.pricechange.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zang.pricechange.common.InputSanitizer;
import com.zang.pricechange.entity.Category;
import com.zang.pricechange.entity.PriceItem;
import com.zang.pricechange.mapper.CategoryMapper;
import com.zang.pricechange.mapper.PriceItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 价格项服务类
 * 处理涨跌幅记录的业务逻辑：创建、查询、删除、更新
 * 涨跌幅计算公式：(目标值 - 当前值) / 当前值 * 100%
 */
@Service
@RequiredArgsConstructor
public class PriceItemService {

    private final PriceItemMapper priceItemMapper;
    private final CategoryMapper categoryMapper;

    /**
     * 创建涨跌幅记录
     */
    public PriceItem create(Long userId, String name, BigDecimal currentValue, BigDecimal targetValue, Long categoryId) {
        if (currentValue == null || currentValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("当前值必须大于0");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }

        // 校验分类归属（如果指定了 categoryId）
        if (categoryId != null) {
            validateCategoryOwnership(userId, categoryId);
        }

        String sanitizedName = InputSanitizer.sanitizeForHtml(name);

        PriceItem item = new PriceItem();
        item.setUserId(userId);
        item.setCategoryId(categoryId);
        item.setName(sanitizedName);
        item.setCurrentValue(currentValue);
        item.setTargetValue(targetValue);

        BigDecimal changePercent = targetValue.subtract(currentValue)
                .divide(currentValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        item.setChangePercent(changePercent);

        priceItemMapper.insert(item);
        return item;
    }

    /**
     * 查询当前用户的所有涨跌幅记录
     */
    public List<PriceItem> list(Long userId, String sortOrder, Long categoryId) {
        LambdaQueryWrapper<PriceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceItem::getUserId, userId);

        // 按分类ID筛选
        if (categoryId != null) {
            wrapper.eq(PriceItem::getCategoryId, categoryId);
        }

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
     */
    public PriceItem update(Long userId, Long id, String name, BigDecimal currentValue, BigDecimal targetValue, Long categoryId) {
        if (currentValue == null || currentValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("当前值必须大于0");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }

        // 校验分类归属（如果指定了 categoryId）
        if (categoryId != null) {
            validateCategoryOwnership(userId, categoryId);
        }

        LambdaQueryWrapper<PriceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceItem::getUserId, userId).eq(PriceItem::getId, id);
        PriceItem existing = priceItemMapper.selectOne(wrapper);
        if (existing == null) {
            throw new IllegalArgumentException("记录不存在或无权修改");
        }

        String sanitizedName = InputSanitizer.sanitizeForHtml(name);
        existing.setName(sanitizedName);
        existing.setCategoryId(categoryId);
        existing.setCurrentValue(currentValue);
        existing.setTargetValue(targetValue);

        BigDecimal changePercent = targetValue.subtract(currentValue)
                .divide(currentValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        existing.setChangePercent(changePercent);

        priceItemMapper.updateById(existing);
        return existing;
    }

    /**
     * 根据分类ID获取分类名称
     */
    public String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryMapper.selectById(categoryId);
        return category != null ? category.getName() : null;
    }

    /**
     * 校验分类归属于当前用户
     */
    private void validateCategoryOwnership(Long userId, Long categoryId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId).eq(Category::getId, categoryId);
        if (categoryMapper.selectCount(wrapper) == 0) {
            throw new IllegalArgumentException("分类不存在或无权使用");
        }
    }
}
