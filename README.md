# CaloriMate（食光机）

智能饮食卡路里管理系统，帮助用户记录每日饮食、追踪卡路里摄入，并提供 AI 营养建议。

## 功能特性

- **用户管理**：注册、登录、个人信息管理（身高/体重/年龄/目标卡路里）
- **食物数据库**：内置 20+ 常见中国食物营养数据，支持自定义添加
- **饮食记录**：按餐次（早餐/午餐/晚餐/加餐）记录每日饮食
- **统计分析**：每日/每周/每月卡路里摄入统计与趋势图表
- **AI 智能解析**：自然语言描述食物，AI 自动识别并估算卡路里
- **AI 营养建议**：基于个人目标和当日摄入，生成个性化营养建议

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Spring Boot 2.7.18 | 应用框架 |
| Spring Security + JWT | 认证与授权 |
| MyBatis-Plus 3.5.3.1 | ORM 框架 |
| H2 / MySQL | 数据库（开发用 H2，生产可切 MySQL） |
| DeepSeek API | AI 能力（自然语言解析 + 营养建议） |
| OkHttp | HTTP 客户端 |
| Lombok | 简化代码 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| Vue Router 4 | 路由管理 |
| Element Plus | UI 组件库 |
| ECharts | 图表可视化 |
| Axios | HTTP 请求 |
| Vite 5 | 构建工具 |

## 项目结构

```
aicode/
├── calorimate-backend/          # 后端 Spring Boot 项目
│   ├── src/main/java/com/calorimate/
│   │   ├── common/              # 通用类（统一返回结果、异常处理）
│   │   ├── config/              # 配置类（Security、MyBatis-Plus、MVC）
│   │   ├── controller/          # 控制器层
│   │   ├── dto/                 # 数据传输对象
│   │   ├── entity/              # 实体类
│   │   ├── interceptor/         # JWT 拦截器
│   │   ├── mapper/              # MyBatis Mapper 接口
│   │   ├── service/             # 业务逻辑层
│   │   ├── util/                # 工具类（JWT）
│   │   └── vo/                  # 视图对象
│   └── src/main/resources/
│       ├── mapper/              # MyBatis XML 映射
│       ├── sql/                 # 数据库初始化脚本
│       └── application.yml      # 应用配置
├── calorimate-frontend/         # 前端 Vue 3 项目
│   ├── src/
│   │   ├── layout/              # 布局组件
│   │   ├── router/              # 路由配置
│   │   ├── utils/               # 工具函数
│   │   └── views/               # 页面组件
│   └── package.json
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+

### 后端启动

```bash
cd calorimate-backend

# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/calorimate-backend-1.0.0.jar
```

后端默认运行在 `http://localhost:8081`

### 前端启动

```bash
cd calorimate-frontend

# 安装依赖
npm install

# 开发模式
npm run dev
```

前端默认运行在 `http://localhost:5173`

### AI 功能配置

在 `application.yml` 中配置 DeepSeek API：

```yaml
ai:
  api-key: your-deepseek-api-key
  base-url: https://api.deepseek.com/v1
  model: deepseek-chat
```

API Key 请在 [DeepSeek Platform](https://platform.deepseek.com/api_keys) 获取。

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | 123456 | 管理员 |

## API 接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/user/register` | 用户注册 | 否 |
| POST | `/api/user/login` | 用户登录 | 否 |
| GET  | `/api/user/info` | 获取用户信息 | JWT |
| GET  | `/api/food/list` | 食物列表 | JWT |
| POST | `/api/food` | 添加食物 | JWT |
| GET  | `/api/diet/list` | 饮食记录列表 | JWT |
| POST | `/api/diet` | 添加饮食记录 | JWT |
| GET  | `/api/stats/daily` | 每日统计 | JWT |
| GET  | `/api/stats/weekly` | 每周统计 | JWT |
| GET  | `/api/stats/monthly` | 每月统计 | JWT |
| POST | `/api/ai/parse` | AI 食物解析 | JWT |
| POST | `/api/ai/advice` | AI 营养建议 | JWT |

## 开发说明

- 开发环境使用 H2 内存数据库，重启数据会重置
- 生产环境切换 MySQL：修改 `application.yml` 中的 datasource 配置，并将 `schema-locations` 和 `data-locations` 指向 MySQL 脚本
- JWT Token 有效期为 24 小时
- H2 控制台：`http://localhost:8081/h2-console`（JDBC URL: `jdbc:h2:mem:calorimate`）
