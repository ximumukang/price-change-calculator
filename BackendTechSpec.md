# 后端技术规范 - Java + Spring Boot

## 1. 项目结构

```
price-change-backend/
├── src/main/java/com/zang/pricechange/
│   ├── controller/    # REST API
│   ├── service/      # 业务逻辑
│   ├── entity/      # 数据实体
│   ├── mapper/      # MyBatis-Plus
│   ├── dto/        # 数据传输对象
│   ├── security/    # 安全组件
│   ├── config/     # 配置类
│   ├── common/    # 通用工具
│   └── annotation/ # 自定义注解
└── src/main/resources/application.yml
```

## 2. 核心依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 框架 |
| Java | 17 | 语言 |
| MyBatis-Plus | 3.5.6 | ORM |
| JJWT | 0.12.5 | JWT 认证 |

## 3. 代码规范

### 3.1 实体类 (Entity)

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    @TableId(type = IdType.ASSIGN_ID)  // 雪花算法
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password_hash")
    private String passwordHash;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

- 主键: `@TableId(type = IdType.ASSIGN_ID)` 雪花算法，禁止 `IdType.AUTO`
- 表名: `@TableName` 映射
- 字段: `@TableField` 映射，自动填充 `fill = FieldFill.INSERT`
- 必须: `@NoArgsConstructor + @AllArgsConstructor`

### 3.2 DTO

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

**请求 DTO**: 验证注解 `@NotBlank`, `@NotNull`, `@Size`
**响应 DTO**: 全参构造器转换

### 3.3 Controller

```java
@RestController
@RequestMapping("/api/price-items")
@RequiredArgsConstructor
@Tag(name = "涨跌幅管理")
public class PriceItemController {
    private final PriceItemService priceItemService;

    @PostMapping
    @Operation(summary = "新增涨跌幅")
    public Result<PriceItemResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PriceItemRequest request) {
        PriceItem item = priceItemService.create(principal.getUserId(), request);
        return Result.success(toResponse(item));
    }

    private PriceItemResponse toResponse(PriceItem item) {
        return new PriceItemResponse(item.getId(), item.getName(),
            item.getCurrentValue(), item.getTargetValue(),
            item.getChangePercent(), item.getCreatedAt());
    }
}
```

- 返回: `Result<T>` 统一响应
- 注解: `@RestController + @RequiredArgsConstructor`

### 3.4 Service

```java
@Service
@RequiredArgsConstructor
public class PriceItemService {
    private final PriceItemMapper priceItemMapper;

    public PriceItem create(Long userId, PriceItemRequest request) {
        if (request.getCurrentValue() == null ||
            request.getCurrentValue().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("当前值必须大于0");
        }
        BigDecimal changePercent = request.getTargetValue()
            .subtract(request.getCurrentValue())
            .divide(request.getCurrentValue(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        // ...
    }
}
```

- 参数校验: 防止除零
- 异常: `IllegalArgumentException`
- 业务逻辑: 必须在 Service 层

## 4. 安全规范

| 类型 | 实现 |
|------|------|
| 密码存储 | BCrypt (`BCryptPasswordEncoder`) |
| 传输加密 | RSA-2048 PKCS1Padding (`RSA/ECB/PKCS1Padding`) |
| 认证 | JWT Token，过期 2 小时 |
| 公开接口 | `SecurityConfig` 配置 `.requestMatchers("/api/auth/**").permitAll()` |

### 公开接口 (无需认证)
- `/api/auth/public-key` - 获取 RSA 公钥
- `/api/auth/register` - 注册
- `/api/auth/login` - 登录

### 受保护接口
- `/api/price-items/**` - 涨跌幅 CRUD
- `/api/categories/**` - 分类 CRUD

## 5. 注意事项

1. **除零风险**: 计算涨跌幅前校验 `currentValue > 0`
2. **表名**: 使用 `users`（`user` 是 SQL 保留字）
3. **Lombok**: `@Data` 配合 `@NoArgsConstructor + @AllArgsConstructor`
4. **日志**: 使用 `@Slf4j`，禁止 `System.out.println`

## 6. API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/auth/public-key | 获取 RSA 公钥 |
| POST | /api/auth/register | 注册 |
| POST | /api/auth/login | 登录 |
| POST | /api/price-items | 新增涨跌幅 |
| GET | /api/price-items?sortOrder=desc | 获取列表 |
| DELETE | /api/price-items/{id} | 删除 |
| POST | /api/categories | 创建分类 |
| GET | /api/categories | 获取分类列表 |
| PUT | /api/categories/{id} | 更新分类 |
| DELETE | /api/categories/{id} | 删除分类 |