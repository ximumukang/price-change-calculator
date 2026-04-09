package com.zang.pricechange.controller;

import com.zang.pricechange.common.Result;
import com.zang.pricechange.dto.PriceItemRequest;
import com.zang.pricechange.dto.PriceItemResponse;
import com.zang.pricechange.entity.PriceItem;
import com.zang.pricechange.security.UserPrincipal;
import com.zang.pricechange.service.PriceItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 价格项控制器
 * 处理涨跌幅记录的增删查接口
 */
@RestController
@RequestMapping("/api/price-items")
@RequiredArgsConstructor
public class PriceItemController {

    private final PriceItemService priceItemService;

    /**
     * 新增涨跌幅记录
     */
    @PostMapping
    public Result<PriceItemResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PriceItemRequest request) {
        PriceItem item = priceItemService.create(
                principal.getUserId(),
                request.getName(),
                request.getCurrentValue(),
                request.getTargetValue()
        );
        return Result.success(toResponse(item));
    }

    /**
     * 查询当前用户的所有涨跌幅记录
     */
    @GetMapping
    public Result<List<PriceItemResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        List<PriceItem> items = priceItemService.list(principal.getUserId(), sortOrder);
        return Result.success(items.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    /**
     * 删除涨跌幅记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        priceItemService.delete(principal.getUserId(), id);
        return Result.success();
    }

    /**
     * 更新涨跌幅记录
     */
    @PutMapping("/{id}")
    public Result<PriceItemResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody PriceItemRequest request) {
        PriceItem item = priceItemService.update(
                principal.getUserId(),
                id,
                request.getName(),
                request.getCurrentValue(),
                request.getTargetValue()
        );
        return Result.success(toResponse(item));
    }

    private PriceItemResponse toResponse(PriceItem item) {
        return new PriceItemResponse(
                item.getId(),
                item.getName(),
                item.getCurrentValue(),
                item.getTargetValue(),
                item.getChangePercent(),
                item.getCreatedAt()
        );
    }
}
