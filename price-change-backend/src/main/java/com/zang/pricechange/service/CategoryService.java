package com.zang.pricechange.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zang.pricechange.common.InputSanitizer;
import com.zang.pricechange.entity.Category;
import com.zang.pricechange.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务类
 * 处理分类的增删改查业务逻辑
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 创建分类
     */
    public Category create(Long userId, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }

        String sanitizedName = InputSanitizer.sanitizeForHtml(name.trim());

        // 校验同一用户下分类名称唯一
        LambdaQueryWrapper<Category> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(Category::getUserId, userId).eq(Category::getName, sanitizedName);
        if (categoryMapper.selectCount(existsWrapper) > 0) {
            throw new IllegalArgumentException("分类名称已存在");
        }

        // 获取当前最大排序值
        LambdaQueryWrapper<Category> maxSortWrapper = new LambdaQueryWrapper<>();
        maxSortWrapper.eq(Category::getUserId, userId)
                .orderByDesc(Category::getSortOrder)
                .last("LIMIT 1");
        Category maxSortCategory = categoryMapper.selectOne(maxSortWrapper);
        int nextSortOrder = (maxSortCategory != null ? maxSortCategory.getSortOrder() : 0) + 1;

        Category category = new Category();
        category.setUserId(userId);
        category.setName(sanitizedName);
        category.setSortOrder(nextSortOrder);

        categoryMapper.insert(category);
        return category;
    }

    /**
     * 获取当前用户的所有分类列表（按排序权重升序）
     */
    public List<Category> list(Long userId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getCreatedAt);
        return categoryMapper.selectList(wrapper);
    }

    /**
     * 更新分类名称
     */
    public Category update(Long userId, Long id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }

        String sanitizedName = InputSanitizer.sanitizeForHtml(name.trim());

        // 校验分类归属
        Category existing = getCategoryById(userId, id);

        // 校验同一用户下分类名称唯一（排除自身）
        LambdaQueryWrapper<Category> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(Category::getUserId, userId)
                .eq(Category::getName, sanitizedName)
                .ne(Category::getId, id);
        if (categoryMapper.selectCount(existsWrapper) > 0) {
            throw new IllegalArgumentException("分类名称已存在");
        }

        existing.setName(sanitizedName);
        categoryMapper.updateById(existing);
        return existing;
    }

    /**
     * 删除分类（关联的 price_item 的 category_id 会被 SET NULL）
     */
    public void delete(Long userId, Long id) {
        Category existing = getCategoryById(userId, id);
        categoryMapper.deleteById(existing.getId());
    }

    /**
     * 根据ID获取分类（校验归属）
     */
    private Category getCategoryById(Long userId, Long id) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId).eq(Category::getId, id);
        Category category = categoryMapper.selectOne(wrapper);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在或无权操作");
        }
        return category;
    }
}
