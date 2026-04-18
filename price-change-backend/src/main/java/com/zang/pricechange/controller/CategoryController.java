package com.zang.pricechange.controller;

import com.zang.pricechange.annotation.AuditLog;
import com.zang.pricechange.common.Result;
import com.zang.pricechange.dto.CategoryRequest;
import com.zang.pricechange.dto.CategoryResponse;
import com.zang.pricechange.entity.Category;
import com.zang.pricechange.security.UserPrincipal;
import com.zang.pricechange.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类控制器
 * 处理分类的增删改查接口
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "分类管理", description = "分类的增删改查接口")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取当前用户的所有分类
     */
    @AuditLog(value = "查询分类列表", operationType = AuditLog.OperationType.QUERY)
    @Operation(summary = "查询分类列表", description = "获取当前用户的所有分类")
    @GetMapping
    public Result<List<CategoryResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Category> categories = categoryService.list(principal.getUserId());
        return Result.success(categories.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    /**
     * 创建新分类
     */
    @AuditLog(value = "创建分类", operationType = AuditLog.OperationType.CREATE)
    @Operation(summary = "创建分类", description = "创建新的分类")
    @PostMapping
    public Result<CategoryResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.create(principal.getUserId(), request.getName());
        return Result.success(toResponse(category));
    }

    /**
     * 更新分类名称
     */
    @AuditLog(value = "更新分类", operationType = AuditLog.OperationType.UPDATE)
    @Operation(summary = "更新分类", description = "更新指定ID的分类名称")
    @PutMapping("/{id}")
    public Result<CategoryResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.update(principal.getUserId(), id, request.getName());
        return Result.success(toResponse(category));
    }

    /**
     * 删除分类
     */
    @AuditLog(value = "删除分类", operationType = AuditLog.OperationType.DELETE)
    @Operation(summary = "删除分类", description = "删除指定ID的分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        categoryService.delete(principal.getUserId(), id);
        return Result.success();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSortOrder(),
                category.getCreatedAt()
        );
    }
}
