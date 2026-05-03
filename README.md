# CaloriMate（食光机）

智能饮食卡路里管理系统，帮助用户记录每日饮食、追踪卡路里摄入，并提供 AI 营养建议。

本项目包含 **两个独立的前端版本**，共享同一个后端服务：

| 版本 | 目录 | 技术栈 | 推送时间 |
|------|------|--------|----------|
| **v1.0 Web 版** | `calorimate-frontend/` | Vue 3 + Element Plus + ECharts | 2026-05-01 |
| **v1.0 微信小程序版** | `miniapp/` | Taro 4 + Vue 3 + TypeScript | 2026-05-02 |

---

## 版本一：Web 版（calorimate-frontend）

> 基于 Vue 3 + Element Plus 的浏览器端应用，适合桌面和移动端浏览器访问。

### 功能特性

- **用户系统**：注册、登录、JWT 令牌认证
- **个人信息管理**：设置身高、体重、年龄、每日卡路里目标
- **食物数据库**：内置 20+ 常见中国食物营养数据，支持自定义添加食物
- **饮食记录**：按餐次（早餐/午餐/晚餐/加餐）记录每日饮食摄入
- **统计分析**：每日/每周/每月卡路里摄入统计，ECharts 趋势图表可视化
- **AI 智能解析**：自然语言描述食物（如"中午吃了一碗米饭和红烧肉"），AI 自动识别并估算卡路里
- **AI 营养建议**：基于个人目标和当日摄入情况，生成个性化营养建议

### 技术栈

| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| Vue Router 4 | 路由管理 |
| Element Plus | UI 组件库 |
| ECharts | 图表可视化 |
| Axios | HTTP 请求 |
| Vite 5 | 构建工具 |

---

## 版本二：微信小程序版（miniapp）

> 基于 Taro 4 + Vue 3 的微信小程序，原生体验，支持拍照识别和微信支付。

### 功能特性

- **微信一键登录**：无需注册，微信授权直接登录，Token 自动管理
- **AI 拍照识别**：调用手机摄像头拍照，AI 自动识别食物种类并估算卡路里
- **AI 文字解析**：自然语言输入食物描述，AI 智能解析营养成分（蛋白质/脂肪/碳水）
- **AI 营养建议**：基于每日目标和实际摄入，生成个性化饮食建议
- **饮食记录管理**：按餐次记录饮食，支持手动添加和 AI 添加，左滑删除
- **每日统计面板**：实时显示当日卡路里摄入/剩余，三大营养素进度环
- **食物搜索**：本地食物库搜索 + 外部网络搜索（兜底策略）
- **个人中心**：
  - 健康数据速览（身高/体重/目标卡路里）
  - 本周概览（日均摄入/体重趋势）
  - 饮食打卡日历（标记有记录的日期）
  - 编辑个人资料（身高/体重/年龄/性别/目标卡路里）
- **VIP 会员订阅**：
  - 包月/年度套餐选择
  - 微信支付集成
  - 会员特权展示（无限 AI 识别/深度营养分析/长期趋势追踪/个性化方案）
  - 开发环境模拟支付调试
- **隐私合规**：首次使用弹出隐私政策弹窗，用户同意后方可使用
- **TabBar 导航**：首页、食物库、拍照识别、个人中心四大模块

### 技术栈

| 技术 | 说明 |
|------|------|
| Taro 4.2.0 | 跨端框架（编译为微信小程序） |
| Vue 3 | 视图层框架 |
| TypeScript | 类型安全 |
| Sass | 样式预处理 |
| Webpack 5 | 构建工具 |

## 后端技术栈

| 技术 | 说明 |
|------|------|
| Spring Boot 2.7.18 | 应用框架 |
| Spring Security + JWT | 认证与授权 |
| MyBatis-Plus 3.5.3.1 | ORM 框架 |
| H2 / MySQL | 数据库（开发用 H2，生产可切 MySQL） |
| DeepSeek API | AI 能力（自然语言解析 + 营养建议） |
| OkHttp | HTTP 客户端 |
| Lombok | 简化代码 |

## 项目结构

```
aicode/
├── calorimate-backend/              # 后端 Spring Boot 项目
│   ├── src/main/java/com/calorimate/
│   │   ├── common/                  # 通用类（统一返回结果、异常处理）
│   │   ├── config/                  # 配置类（Security、MyBatis-Plus、MVC）
│   │   ├── controller/              # 控制器层
│   │   ├── dto/                     # 数据传输对象
│   │   ├── entity/                  # 实体类
│   │   ├── interceptor/             # JWT 拦截器
│   │   ├── mapper/                  # MyBatis Mapper 接口
│   │   ├── service/                 # 业务逻辑层
│   │   ├── util/                    # 工具类（JWT）
│   │   └── vo/                      # 视图对象
│   └── src/main/resources/
│       ├── mapper/                  # MyBatis XML 映射
│       ├── sql/                     # 数据库初始化脚本
│       └── application.yml          # 应用配置
├── calorimate-frontend/             # Web 前端 Vue 3 项目
│   ├── src/
│   │   ├── layout/                  # 布局组件
│   │   ├── router/                  # 路由配置
│   │   ├── utils/                   # 工具函数
│   │   └── views/                   # 页面组件
│   └── package.json
├── miniapp/                         # 微信小程序项目
│   ├── src/
│   │   ├── api/                     # 接口请求封装
│   │   ├── assets/                  # 静态资源（图标等）
│   │   ├── components/              # 公共组件
│   │   ├── pages/                   # 页面
│   │   │   ├── index/               # 首页（每日饮食概览）
│   │   │   ├── food/                # 食物数据库
│   │   │   ├── camera/              # 拍照识别
│   │   │   ├── profile/             # 个人中心 + 编辑资料
│   │   │   ├── vip/                 # 会员订阅
│   │   │   └── login/               # 微信登录
│   │   ├── styles/                  # 全局样式
│   │   ├── types/                   # TypeScript 类型定义
│   │   └── utils/                   # 工具函数
│   ├── project.config.json          # 微信开发者工具配置
│   └── package.json
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+
- 微信开发者工具（小程序版）

### 后端启动

```bash
cd calorimate-backend

# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/calorimate-backend-1.0.0.jar
```

后端默认运行在 `http://localhost:8081`

### Web 前端启动

```bash
cd calorimate-frontend

# 安装依赖
npm install

# 开发模式
npm run dev
```

Web 前端默认运行在 `http://localhost:5173`

### 微信小程序启动

```bash
cd miniapp

# 安装依赖
npm install

# 编译（微信小程序）
npm run dev:weapp
```

编译产物在 `miniapp/dist/` 目录，使用**微信开发者工具**导入该目录即可预览调试。

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
| POST | `/api/user/login` | 用户登录（Web 端） | 否 |
| POST | `/api/user/wx-login` | 微信登录（小程序端） | 否 |
| GET  | `/api/user/info` | 获取用户信息 | JWT |
| PUT  | `/api/user/update` | 更新个人信息 | JWT |
| GET  | `/api/food/list` | 食物列表 | JWT |
| POST | `/api/food` | 添加食物 | JWT |
| GET  | `/api/food/search` | 食物搜索 | JWT |
| GET  | `/api/diet/list` | 饮食记录列表 | JWT |
| POST | `/api/diet` | 添加饮食记录 | JWT |
| GET  | `/api/stats/daily` | 每日统计 | JWT |
| GET  | `/api/stats/weekly` | 每周统计 | JWT |
| GET  | `/api/stats/monthly` | 每月统计 | JWT |
| POST | `/api/ai/parse` | AI 食物解析 | JWT |
| POST | `/api/ai/recognize` | AI 拍照识别食物 | JWT |
| POST | `/api/ai/advice` | AI 营养建议 | JWT |
| POST | `/api/pay/prepay` | 创建支付订单 | JWT |
| POST | `/api/pay/notify` | 支付回调通知 | 否 |

## 开发说明

- 开发环境使用 H2 内存数据库，重启数据会重置
- 生产环境切换 MySQL：修改 `application.yml` 中的 datasource 配置，并将 `schema-locations` 和 `data-locations` 指向 MySQL 脚本
- JWT Token 有效期为 24 小时
- H2 控制台：`http://localhost:8081/h2-console`（JDBC URL: `jdbc:h2:mem:calorimate`）
- 微信小程序需要在 `project.config.json` 中配置自己的 AppID
- 小程序 API 请求域名需要在微信公众平台配置合法域名
