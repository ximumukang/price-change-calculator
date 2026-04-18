package com.zang.pricechange.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
