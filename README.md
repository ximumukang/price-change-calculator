# Price Change Calculator

涨跌幅计算应用，采用前后端分离架构。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis-Plus + MySQL |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia |
| 认证 | JWT + RSA 加密传输 |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 后端

```bash
cd price-change-backend
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 前端

```bash
cd price-change-frontend
npm install
npm run dev
```

前端运行在 http://localhost:5173

### 数据库

创建 MySQL 数据库 `price_change`，后端启动时 MyBatis-Plus 会自动建表。

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/public-key` | 获取 RSA 公钥 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/price-items` | 新增涨跌幅记录 |
| GET | `/api/price-items?sortOrder=desc` | 获取列表 |
| DELETE | `/api/price-items/{id}` | 删除记录 |

## 项目结构

```
├── price-change-backend/          # Spring Boot 后端
│   ├── controller/                # 控制器
│   ├── service/                   # 业务逻辑
│   ├── mapper/                    # MyBatis-Plus Mapper
│   ├── entity/                    # 实体类
│   ├── dto/                       # 数据传输对象
│   ├── security/                  # JWT / RSA 安全模块
│   ├── config/                    # 配置类
│   └── common/                    # 统一响应、异常处理
└── price-change-frontend/         # Vue 前端
    ├── src/api/                   # API 调用
    ├── src/views/                 # 页面组件
    ├── src/store/                 # Pinia 状态管理
    ├── src/router/                # 路由
    └── src/utils/                 # 工具函数
```

## 安全

- 密码使用 BCrypt 加密存储
- 登录密码通过 RSA-2048 加密传输
- 使用 JWT Token 认证，默认 2 小时过期
