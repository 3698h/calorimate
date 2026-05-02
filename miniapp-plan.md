# CaloriMate 微信小程序迁移计划

## 一、现有代码复用分析

### 1.1 后端 API 复用（100% 可复用）

现有后端 14 个 REST API 全部为标准 JSON 接口，微信小程序可直接调用，无需修改后端代码。

| 模块 | 接口数 | 复用度 | 说明 |
|------|--------|--------|------|
| 用户模块 | 3 | 100% | 微信登录需新增接口 |
| 食物模块 | 5 | 100% | 直接复用 |
| 饮食记录模块 | 4 | 100% | 直接复用 |
| 统计模块 | 3 | 100% | 直接复用 |
| AI 模块 | 2 | 100% | 直接复用 |

### 1.2 前端代码复用分析

| 维度 | Web 端 | 小程序端 | 复用策略 |
|------|--------|----------|----------|
| 请求封装 | Axios (request.js) | Taro.request | 逻辑复用，API 层重写 |
| 路由 | Vue Router | Taro 路由 | 配置方式不同，需重写 |
| 状态管理 | localStorage | wx.setStorageSync | 接口类似，可封装 |
| UI 组件 | Element Plus | NutUI (Taro 版) | 组件名映射 |
| 图表 | ECharts | echarts-for-weixin | 配置项可复用 |
| 认证逻辑 | JWT Bearer Token | JWT Bearer Token | 完全复用 |

### 1.3 不可复用部分

- **登录方式**：Web 端用户名密码登录 → 小程序端微信授权登录
- **布局系统**：侧边栏布局 → 底部 TabBar 布局
- **表格组件**：Element Plus Table → 小程序列表/卡片
- **日期选择器**：Element Plus DatePicker → 小程序原生 picker

---

## 二、小程序页面结构树

```
miniapp/
├── src/
│   ├── app.config.ts              # 全局配置（页面、窗口、TabBar、权限）
│   ├── app.ts                     # 应用入口
│   ├── app.scss                   # 全局样式
│   ├── pages/
│   │   ├── login/
│   │   │   └── index.vue          # 微信登录页
│   │   ├── index/
│   │   │   └── index.vue          # 首页数据看板（Tab 1）
│   │   ├── food/
│   │   │   └── index.vue          # 食物搜索与记录（Tab 2）
│   │   ├── camera/
│   │   │   └── index.vue          # AI 拍照识别（Tab 3）
│   │   ├── profile/
│   │   │   └── index.vue          # 个人中心（Tab 4）
│   │   └── vip/
│   │       └── index.vue          # 会员购买页
│   ├── components/
│   │   ├── FoodCard.vue           # 食物卡片组件
│   │   ├── MealGroup.vue          # 餐次分组组件
│   │   ├── CalorieRing.vue        # 卡路里环形图组件
│   │   ├── NutrientBar.vue        # 营养素进度条组件
│   │   └── AiResultCard.vue       # AI 解析结果卡片
│   ├── api/
│   │   ├── request.ts             # Taro.request 封装（Token、401 处理）
│   │   ├── user.ts                # 用户相关 API
│   │   ├── food.ts                # 食物相关 API
│   │   ├── diet.ts                # 饮食记录 API
│   │   ├── stats.ts               # 统计 API
│   │   └── ai.ts                  # AI 功能 API
│   ├── utils/
│   │   ├── storage.ts             # 本地存储封装
│   │   └── format.ts              # 日期/数字格式化
│   └── types/
│       └── index.ts               # TypeScript 类型定义
├── config/
│   ├── index.ts                   # 编译配置
│   └── dev.ts                     # 开发环境配置
├── project.config.json            # 微信开发者工具配置
├── package.json
├── tsconfig.json
└── babel.config.js
```

---

## 三、后端 API 改造清单

### 3.1 新增接口

| 方法 | 路径 | 说明 | 优先级 |
|------|------|------|--------|
| POST | `/api/user/wx-login` | 微信登录（code 换 openid + JWT） | P0 |
| POST | `/api/ai/recognize` | 图片识别（接收图片 base64，返回食物列表） | P1 |
| POST | `/api/order/create` | 创建会员订单 | P2 |
| GET  | `/api/user/vip-status` | 查询会员状态 | P2 |

### 3.2 现有接口改造

| 接口 | 改造内容 | 原因 |
|------|----------|------|
| `POST /api/user/register` | 新增 `wxOpenId` 字段 | 微信用户绑定 |
| `GET /api/user/info` | 返回增加 `vipLevel`, `vipExpireTime` | 会员信息 |
| `POST /api/ai/parse` | 支持图片输入（base64） | 拍照识别 |
| CORS 配置 | 允许小程序域名 | 微信安全域名要求 |

### 3.3 数据库改造

```sql
-- user 表新增字段
ALTER TABLE user ADD COLUMN wx_openid VARCHAR(64);
ALTER TABLE user ADD COLUMN vip_level INT DEFAULT 0;
ALTER TABLE user ADD COLUMN vip_expire_time TIMESTAMP;

-- 新增订单表
CREATE TABLE vip_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    wx_transaction_id VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 四、技术选型

| 维度 | 选型 | 说明 |
|------|------|------|
| 框架 | Taro 3.x + Vue 3 | 跨端框架，Vue 语法 |
| 语言 | TypeScript | 类型安全 |
| UI 库 | NutUI (Taro 版) | 京东出品，适配 Taro |
| 图表 | echarts-for-weixin | ECharts 小程序版 |
| 状态管理 | Vue 3 reactive | 轻量级，无需额外库 |
| 构建 | Webpack 5 | Taro 默认 |

---

## 五、开发排期

| 阶段 | 内容 | 工作量 |
|------|------|--------|
| Phase 1 | 项目初始化 + 登录页 + 请求封装 | 1 天 |
| Phase 2 | 首页看板 + 食物搜索 | 2 天 |
| Phase 3 | 饮食记录 + AI 拍照识别 | 2 天 |
| Phase 4 | 个人中心 + 会员购买 | 1 天 |
| Phase 5 | 后端改造（微信登录 + 图片识别） | 2 天 |
| Phase 6 | 联调测试 + 优化 | 2 天 |
