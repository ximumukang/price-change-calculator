# AGENTS.md - 涨跌幅计算项目开发指南

本文档为 Agentic Coding 代理提供项目开发规范。

---

## 1. 项目概述

本项目是一个「涨跌幅计算」应用，采用前后端分离架构：

- **后端**: Java 17 + Spring Boot 3.2 + MyBatis-Plus + MySQL
- **前端**: Vue 3 + TypeScript + Vite + Element Plus + Pinia

### 目录结构

```
D:\zang\
├── price-change-backend/          # Spring Boot 后端 (端口 8080)
│   ├── src/main/java/com/zang/pricechange/
│   │   ├── PriceChangeApplication.java    # 启动入口
│   │   ├── config/                        # 配置类
│   │   │   ├── MyBatisPlusConfig.java     # MyBatis-Plus 配置（分页、自动填充）
│   │   │   └── SecurityConfig.java        # Spring Security 配置
│   │   ├── controller/                    # 控制器
│   │   │   ├── AuthController.java        # 认证接口（登录/注册/公钥）
│   │   │   └── PriceItemController.java   # 涨跌幅 CRUD 接口
│   │   ├── entity/                        # 实体类
│   │   │   ├── User.java                  # 用户表映射
│   │   │   └── PriceItem.java             # 价格项表映射
│   │   ├── mapper/                        # MyBatis-Plus Mapper
│   │   │   ├── UserMapper.java
│   │   │   └── PriceItemMapper.java
│   │   ├── service/                       # 业务逻辑
│   │   │   ├── UserService.java
│   │   │   └── PriceItemService.java
│   │   ├── dto/                           # 数据传输对象
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   ├── PublicKeyResponse.java
│   │   │   ├── PriceItemRequest.java
│   │   │   └── PriceItemResponse.java
│   │   ├── security/                      # 安全相关
│   │   │   ├── RsaUtils.java              # RSA 加解密（OAEP 填充）
│   │   │   ├── JwtTokenProvider.java      # JWT 生成/验证
│   │   │   ├── JwtAuthenticationFilter.java  # JWT 过滤器
│   │   │   └── UserPrincipal.java         # 当前用户信息
│   │   └── common/                        # 通用类
│   │       ├── Result.java                # 统一响应封装
│   │       └── GlobalExceptionHandler.java  # 全局异常处理
│   ├── src/main/resources/
│   │   └── application.yml         # 应用配置
│   └── pom.xml                     # Maven 依赖
└── price-change-frontend/          # Vue 前端 (端口 5173)
    ├── src/
    │   ├── api/                    # API 调用
    │   │   ├── auth.ts
    │   │   └── priceItem.ts
    │   ├── router/                 # 路由
    │   │   └── index.ts
    │   ├── store/                  # Pinia 状态管理
    │   │   └── auth.ts
    │   ├── utils/                  # 工具函数
    │   │   ├── request.ts          # Axios 封装
    │   │   └── crypto.ts           # RSA 加密
    │   ├── views/                  # 页面组件
    │   │   ├── Login.vue
    │   │   ├── Register.vue
    │   │   └── Dashboard.vue
    │   ├── App.vue
    │   └── main.ts
    ├── public/
    ├── index.html
    ├── vite.config.ts
    ├── package.json
    └── tsconfig.json
```

---

## 2. 构建与测试命令

### 后端 (Maven)

运行后端服务时 用./start-backend.ps1

```powershell
cd D:\zang\price-change-backend

# 编译
mvn clean compile

# 打包
mvn package -DskipTests


# 运行 JAR
java -jar target/price-change-backend-1.0.0.jar

# 测试
mvn test
mvn test -Dtest=TestClassName
mvn test -Dtest=TestClassName#testMethodName

# 测试报告
mvn surefire:report
```

### 前端 (Vite/NPM)

```powershell
cd D:\zang\price-change-frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build

# 预览构建
npm run preview

# 类型检查
npx tsc --noEmit
```

---

## 3. 代码风格规范

### 3.1 后端 (Java)

#### 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 类名 | UpperCamelCase | `UserService`, `PriceItemController` |
| 方法名 | lowerCamelCase | `getUserById`, `calculateChange` |
| 变量名 | lowerCamelCase | `userName`, `priceList` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 包名 | lowercase | `com.zang.pricechange.config` |

#### 实体类 (Entity)

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("username")
    private String username;
    
    @TableField("password_hash")
    private String passwordHash;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

- 使用 `@TableName` 指定表名
- 使用 `@TableField` 映射字段名，自动填充字段使用 `fill = FieldFill.INSERT`
- 主键使用 `@TableId(type = IdType.AUTO)` 自增
- 必须添加 `@NoArgsConstructor` 和 `@AllArgsConstructor` (Lombok)
- `@Data` 自动生成 getter/setter/toString/equals/hashCode

#### DTO 规范

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

- 请求 DTO 使用 `@NotBlank`, `@NotNull`, `@Size` 等验证注解
- 必须添加 `@NoArgsConstructor` 和 `@AllArgsConstructor`
- 响应 DTO 使用全参构造器简化转换

#### Controller 规范

```java
@RestController
@RequestMapping("/api/price-items")
@RequiredArgsConstructor
public class PriceItemController {
    private final PriceItemService priceItemService;
    
    @PostMapping
    public Result<PriceItemResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PriceItemRequest request) {
        PriceItem item = priceItemService.create(...);
        return Result.success(toResponse(item));
    }
    
    // Entity -> DTO 转换使用全参构造器
    private PriceItemResponse toResponse(PriceItem item) {
        return new PriceItemResponse(
                item.getId(), item.getName(), item.getCurrentValue(),
                item.getTargetValue(), item.getChangePercent(), item.getCreatedAt());
    }
}
```

- 使用 `@RestController` 而非 `@Controller`
- 使用 `@RequiredArgsConstructor` 依赖注入
- 返回 `Result<T>` 统一响应格式
- Entity -> DTO 转换使用全参构造器，避免逐字段 setter

#### Service 规范

```java
@Service
@RequiredArgsConstructor
public class PriceItemService {
    private final PriceItemMapper priceItemMapper;
    
    public PriceItem create(Long userId, String name, BigDecimal currentValue, BigDecimal targetValue) {
        // 参数校验：防止除零
        if (currentValue == null || currentValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("当前值必须大于0");
        }
        // 业务逻辑...
    }
}
```

- 使用 `@RequiredArgsConstructor` 替代手动构造器
- 关键业务方法需做参数校验
- 业务逻辑必须在 Service 层

#### 安全规范

- 密码使用 BCrypt 加密存储 (`BCryptPasswordEncoder`)
- 传输使用 RSA-2048 + PKCS1Padding 填充加密 (`RSA/ECB/PKCS1Padding`)
- 使用 JWT Token 认证，过期时间默认 2 小时
- JWT 过滤器需检查 `SecurityContext` 是否已有认证信息，避免重复处理

#### 日志规范

- 使用 `Slf4j` 记录日志，禁止 `System.out.println`
- `GlobalExceptionHandler` 需记录所有异常日志
- `JwtTokenProvider` 需区分记录 Token 过期、无效等不同级别日志
- `JwtAuthenticationFilter` 认证成功记录 `debug` 级别日志

#### 异常处理

- 参数校验失败使用 `IllegalArgumentException`
- 业务异常使用 `RuntimeException`（带明确错误信息）
- `GlobalExceptionHandler` 统一捕获并返回 `Result.error()` 格式

---

### 3.2 前端 (Vue 3 + TypeScript)

#### 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `Login.vue`, `Dashboard.vue` |
| 工具函数 | camelCase | `crypto.ts`, `request.ts` |
| API 模块 | camelCase | `auth.ts`, `priceItem.ts` |
| 常量 | UPPER_SNAKE_CASE | `API_BASE_URL` |
| 类型/接口 | PascalCase | `User`, `LoginRequest` |

#### 组件规范

```vue
<template>
  <el-form :model="form" :rules="rules" ref="formRef">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

interface LoginForm {
  username: string
  password: string
}

const formRef = ref<FormInstance>()
const form = reactive<LoginForm>({
  username: '',
  password: ''
})

const rules = reactive<FormRules<LoginForm>>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})
</script>
```

- 使用 `<script setup lang="ts">`
- 定义接口类型
- 使用 `reactive` 定义表单数据
- 使用 `ref` 引用 DOM

#### API 调用规范

```typescript
// src/api/auth.ts
import request from '@/utils/request'
import type { LoginRequest, LoginResponse } from '@/types'

export function login(data: LoginRequest) {
  return request.post<LoginResponse>('/api/auth/login', data)
}
```

- 使用 `@/utils/request` 封装 axios
- 定义请求/响应类型

#### 路由规范

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (!authStore.token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
```

---

## 4. 配置说明

### 环境变量配置

**重要**: 敏感信息（数据库密码、JWT 密钥、RSA 密钥）必须通过环境变量配置，禁止硬编码在配置文件中。

项目根目录提供 `.env.example` 模板文件，复制为 `.env` 并填入实际值：

```bash
cp .env.example .env
# 编辑 .env 文件填入实际值
```

**必需的环境变量**:

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `<强密码>` |
| `JWT_SECRET` | JWT 签名密钥（至少 256 位 Base64） | `openssl rand -base64 32` |
| `JWT_EXPIRATION` | Token 过期时间（毫秒） | `7200000` |
| `RSA_PRIVATE_KEY` | RSA 私钥（Base64 编码 PKCS#8） | `<生成的私钥>` |
| `RSA_PUBLIC_KEY` | RSA 公钥（Base64 编码 X.509） | `<生成的公钥>` |

**注意**: `.env` 文件已加入 `.gitignore`，不会被提交到版本控制系统。

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/price_change?...
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:7200000}

app:
  cors:
    allowed-origins: http://localhost:5173,http://localhost:3000
  rsa:
    private-key: ${RSA_PRIVATE_KEY}
    public-key: ${RSA_PUBLIC_KEY}
```

### 依赖版本

| 依赖 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Java | 17 |
| MyBatis-Plus | 3.5.6 |
| JJWT | 0.12.5 |
| MySQL Connector | (Spring Boot 管理) |

---

## 5. 常见问题与注意事项

### 后端

1. **数据库**: 使用 MySQL，表名 `users`（`user` 是 SQL 保留字）
2. **计算列**: 数据库不支持 `GENERATED ALWAYS AS`，涨跌幅计算在 Java 层完成
3. **Lombok**: `@Data` 注解的 DTO 必须添加 `@NoArgsConstructor` 和 `@AllArgsConstructor`
4. **Spring Security**: 公开接口需在 `SecurityConfig` 中配置 `.permitAll()`
5. **除零风险**: 涨跌幅计算前必须校验 `currentValue > 0`
6. **RSA 加密**: 使用 PKCS1Padding 填充模式 (`RSA/ECB/PKCS1Padding`)，前后端需一致（前端 jsencrypt 仅支持 PKCS1Padding）
7. **JWT 解析**: 提取公共 `parseClaims()` 方法，避免重复代码
8. **CORS 配置**: 允许域名通过 `app.cors.allowed-origins` 配置，不要硬编码

### 前端

1. **RSA 加密**: 前端使用 `jsencrypt` 加密密码后传输
2. **API 代理**: Vite 配置 `/api` 代理到 `http://localhost:8080`
3. **Token 存储**: 使用 Pinia store，持久化到 localStorage
4. **类型推断**: 优先使用 TypeScript 类型推断，避免 `any`

---

## 6. 测试建议

### 后端测试

- 使用 `@SpringBootTest` 进行集成测试
- 使用 `@MockBean` 进行单元测试
- 测试文件放在 `src/test/java/`
- 重点测试：涨跌幅计算精度、除零保护、Token 过期验证

### 前端测试

- 可使用 Vitest + Vue Test Utils
- 单元测试文件放在 `src/**/*.test.ts`

---

## 7. 开发流程

1. **后端启动**:  用./start-backend.ps1` (端口 8080)
2. **前端启动**: `npm run dev` (端口 5173)
3. **访问**: http://localhost:5173
4. **测试 API**:
   - `GET /api/auth/public-key` - 获取 RSA 公钥
   - `POST /api/auth/register` - 注册
   - `POST /api/auth/login` - 登录
   - `POST /api/price-items` - 新增涨跌幅记录
   - `GET /api/price-items?sortOrder=desc` - 获取列表
   - `DELETE /api/price-items/{id}` - 删除记录

---


