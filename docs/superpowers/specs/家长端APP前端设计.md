# 家长APP前端设计文档

## 概述

疫苗管理系统V2的家长端APP，基于uni-app框架构建，支持H5、微信小程序和APP多端编译。家长通过此APP管理儿童档案、预约接种、查看流程指引、浏览疫苗目录和系统公告。

## 技术栈

| 项目 | 选型 |
|------|------|
| 框架 | uni-app + Vue 3 (Composition API / `<script setup>`) |
| 状态管理 | Pinia |
| UI组件库 | uni-ui（原架构设计为 uView UI 2.x，因 uni-ui 与 uni-app 原生兼容性更好、维护更活跃，三端统一变更为 uni-ui） |
| HTTP请求 | uni.request 封装，带JWT拦截器 |
| 目标平台 | H5 + 微信小程序 + APP |
| 样式 | SCSS + uni.scss变量 |

## 项目结构（标准分层）

```
vaccine_systemV2app/
├── api/                  # API接口层（按模块拆分）
│   ├── auth.js           # 认证相关接口
│   ├── user.js           # 用户信息接口
│   ├── child.js          # 儿童档案接口
│   ├── appointment.js    # 预约接口
│   ├── vaccine.js        # 疫苗目录接口
│   ├── notice.js         # 公告接口
│   └── record.js         # 接种记录接口
├── components/           # 可复用业务组件
├── hooks/                # Vue 3组合式函数
│   ├── useAuth.js        # 登录/注册/登出
│   ├── useAppointment.js # 预约CRUD
│   ├── useChild.js       # 儿童档案CRUD
│   └── usePagination.js  # 通用分页
├── pages/                # 页面组件
│   ├── index/            # 首页（TabBar）
│   ├── appointment/      # 预约相关
│   ├── auth/             # 认证相关
│   ├── child/            # 儿童档案
│   ├── mine/             # 个人中心（TabBar）
│   ├── vaccine/          # 疫苗目录
│   ├── notice/           # 公告
│   └── record/           # 接种记录
├── static/               # 静态资源
├── store/                # Pinia状态管理
│   ├── index.js          # Pinia实例
│   ├── user.js           # 用户认证状态
│   ├── child.js          # 儿童列表缓存
│   └── appointment.js    # 预约状态
├── utils/                # 工具函数
│   ├── request.js        # HTTP请求封装
│   ├── auth.js           # 认证工具
│   └── constants.js      # 常量定义
├── App.vue               # 应用入口
├── main.js               # Vue实例初始化
├── pages.json            # 页面路由配置
├── manifest.json         # 应用配置
└── uni.scss              # 全局样式变量
```

## 页面路由设计

### TabBar（3个Tab）

| Tab | 路径 | 图标 | 说明 |
|-----|------|------|------|
| 首页 | `pages/index/index` | home | 今日预约概览、快捷入口、公告轮播 |
| 预约 | `pages/appointment/list` | calendar | 预约列表（按状态筛选） |
| 我的 | `pages/mine/index` | person | 个人中心、儿童管理、设置 |

### 非Tab页面

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `pages/auth/login` | 登录（手机号+密码 / 短信验证码） |
| 认证 | `pages/auth/register` | 注册 |
| 认证 | `pages/auth/forgot-password` | 忘记密码 |
| 认证 | `pages/auth/change-password` | 修改密码 |
| 儿童 | `pages/child/list` | 儿童列表 |
| 儿童 | `pages/child/add` | 添加/编辑儿童 |
| 预约 | `pages/appointment/create` | 创建预约（选疫苗→选日期→选时段） |
| 预约 | `pages/appointment/detail` | 预约详情 |
| 预约 | `pages/appointment/guide` | 流程指引 |
| 疫苗 | `pages/vaccine/list` | 疫苗目录浏览 |
| 疫苗 | `pages/vaccine/detail` | 疫苗详情 |
| 公告 | `pages/notice/list` | 公告列表 |
| 公告 | `pages/notice/detail` | 公告详情 |
| 公告 | `pages/notice/feedback` | 公告反馈 |
| 记录 | `pages/record/list` | 接种记录 |

## 基础设施层设计

### HTTP请求封装 (`utils/request.js`)

- 统一baseURL配置（从环境变量或配置读取）
- 请求拦截器：自动注入 `Authorization: Bearer <token>` header
- 响应拦截器：
  - `code=200` → 返回 `data`
  - `code=401` → 跳转登录页，清除token
  - 其他code → `uni.showToast` 显示 `message`
- 写操作自动生成 `X-Request-Id`（UUID v4），保证幂等性
- 支持 `showLoading` 参数控制loading状态

### 认证工具 (`utils/auth.js`)

- `getToken()` / `setToken(token)` / `removeToken()` — 使用 `uni.setStorageSync`
- `isLoggedIn()` — 检查token是否存在
- `logout()` — 清除token + Pinia状态 + 跳转登录页

### Pinia Store

#### `useUserStore`
- State: `token`, `userInfo` (id, phone, realName, gender, idCardNo), `isLoggedIn`
- Actions: `login()`, `logout()`, `fetchProfile()`, `updateProfile()`

#### `useChildStore`
- State: `children[]`, `currentChild`
- Actions: `fetchChildren()`, `addChild()`, `updateChild()`, `deleteChild()`

#### `useAppointmentStore`
- State: `appointments[]`, `currentAppointment`, `filters` (status, page)
- Actions: `fetchAppointments()`, `createAppointment()`, `cancelAppointment()`, `fetchDetail()`, `fetchGuide()`

### Hooks

| Hook | 用途 |
|------|------|
| `useAuth` | 封装登录/注册/登出逻辑，返回loading状态和错误信息 |
| `useAppointment` | 封装预约CRUD、取消，返回列表和操作方法 |
| `useChild` | 封装儿童档案CRUD，返回列表和操作方法 |
| `usePagination` | 通用分页：page, size, total, hasMore, loadMore(), reset() |

### API层 (`api/`)

#### `api/auth.js`
- `POST /api/v1/public/sms/send` — 发送短信验证码
- `POST /api/v1/public/auth/login` — 登录
- `POST /api/v1/user/register` — 注册
- `POST /api/v1/user/logout` — 登出
- `PUT /api/v1/user/password` — 修改密码
- `POST /api/v1/user/password/reset` — 重置密码

#### `api/user.js`
- `GET /api/v1/user/profile` — 获取用户信息
- `PUT /api/v1/user/profile` — 更新用户信息

#### `api/child.js`
- `GET /api/v1/user/children` — 儿童列表
- `POST /api/v1/user/children` — 添加儿童
- `PUT /api/v1/user/children/{childId}` — 更新儿童
- `DELETE /api/v1/user/children/{childId}` — 删除儿童

#### `api/appointment.js`
- `POST /api/v1/user/appointments` — 创建预约
- `POST /api/v1/user/appointments/{id}/cancel` — 取消预约
- `GET /api/v1/user/appointments` — 预约列表
- `GET /api/v1/user/appointments/{id}` — 预约详情
- `GET /api/v1/user/appointments/{id}/guide` — 流程指引
- `GET /api/v1/user/appointments/{id}/queue` — 排队信息

#### `api/vaccine.js`
- `GET /api/v1/public/vaccines` — 疫苗目录（公开）

#### `api/notice.js`
- `GET /api/v1/public/notices` — 公告列表
- `POST /api/v1/public/notices/{noticeId}/feedback` — 提交反馈

#### `api/record.js`
- `GET /api/v1/user/records` — 接种记录列表（分页）
- `GET /api/v1/user/records/child/{childId}` — 指定儿童接种记录

## 核心页面交互设计

### 登录页 (`pages/auth/login`)
- 两种方式：手机号+密码 / 手机号+短信验证码（Tab切换）
- 短信验证码：60秒倒计时按钮
- 登录成功 → 存token → 跳转首页
- 底部链接：注册入口、忘记密码入口

### 首页 (`pages/index/index`)
- 顶部：用户问候语 + 今日日期
- 公告轮播（横向滚动swiper）
- 今日预约卡片（如有预约，显示状态+流程指引入口）
- 快捷入口网格（2x2）：预约接种、儿童管理、疫苗目录、接种记录
- 系统公告入口

### 创建预约 (`pages/appointment/create`)
- 步骤条（3步）：
  1. **选择儿童** — 横向卡片选择已有儿童，或"添加儿童"入口
  2. **选择疫苗** — 疫苗列表（按一类/二类分类），显示名称+厂家+描述
  3. **选择时间** — 日历组件选日期（最多提前7天）+ 时段列表
- 底部固定：确认预约按钮
- 提交后显示预约成功页（预约编号、日期时间、注意事项）

### 预约列表 (`pages/appointment/list`)
- 顶部Tab筛选：全部 / 待签到 / 进行中 / 已完成 / 已取消
- 卡片列表：疫苗名称、日期时段、儿童姓名、状态标签
- 下拉刷新 + 上拉加载更多
- 点击卡片 → 预约详情
- 浮动"+"按钮 → 创建预约

### 预约详情 (`pages/appointment/detail`)
- 顶部状态卡片（颜色区分：待签到-蓝色、进行中-橙色、已完成-绿色、已取消-灰色）
- 信息区：疫苗名称、接种日期、时段、儿童姓名、预约编号
- 底部操作按钮：
  - 待签到 → 取消预约
  - 已签到/预检/登记/接种/留观 → 查看流程指引
  - 已完成/已取消 → 仅查看

### 流程指引 (`pages/appointment/guide`)
- 6步流程图（签到→预检→登记→接种→留观→完成）
- 当前步骤高亮，已完成步骤打勾
- 当前窗口信息（窗口号、排队位置、预计等待时间）
- 定时刷新排队信息（每30秒）

### 儿童管理 (`pages/child/list`)
- 儿童卡片列表（姓名、性别、年龄、身份证号脱敏）
- 右上角"添加"按钮（最多5人提示）
- 点击卡片 → 查看/编辑
- 左滑删除（无进行中预约才可删）

### 添加/编辑儿童 (`pages/child/add`)
- 表单字段：姓名、性别（选择器）、出生日期（日期选择器）、身份证号、身份证类型（选择器）
- 必填校验：姓名、性别、出生日期、身份证号
- 编辑模式：传入childId，表单预填

### 疫苗目录 (`pages/vaccine/list`)
- 顶部搜索框
- 分类Tab：全部 / 一类疫苗 / 二类疫苗
- 疫苗卡片：名称、类型标签、厂家、简介
- 点击 → 疫苗详情

### 个人中心 (`pages/mine/index`)
- 用户信息卡片（头像、姓名、手机号脱敏）
- 菜单列表：儿童管理、修改密码、关于系统
- 退出登录按钮（二次确认）

## 预约状态与颜色映射

| 状态码 | 状态名 | 颜色 |
|--------|--------|------|
| 1 | APPOINTED（已预约/待签到） | #1989fa 蓝色 |
| 2 | COMPLETED（已完成） | #07c160 绿色 |
| 3 | CANCELLED（已取消） | #999999 灰色 |
| 4 | EXPIRED（已过期） | #999999 灰色 |
| 6 | SIGNED_IN（已签到） | #ff9900 橙色 |
| 7 | PRECHECK_PASS（预检通过） | #ff9900 橙色 |
| 8 | REGISTERED（已登记） | #ff9900 橙色 |
| 9 | PRECHECK_FAIL（预检未通过） | #ee0a24 红色 |
| 10 | OBSERVING（留观中） | #ff9900 橙色 |

## UI视觉设计规范

### 设计风格

采用微信小程序官方设计风格：简洁留白、圆角卡片、轻量扁平、信息层级清晰。

### 配色体系

所有颜色变量统一定义在 `uni.scss` 中，页面通过 SCSS 变量引用，不硬编码色值。

#### 主色（Primary）

| 变量名 | 色值 | 用途 |
|--------|------|------|
| `$color-primary` | `#07C160` | 品牌主色，用于主要按钮、TabBar选中、链接 |
| `$color-primary-light` | `#07C160` 10%透明度 | 按钮按压态背景 |
| `$color-primary-dark` | `#06AD56` | 主色深色变体，按钮hover态 |

#### 辅助色（Secondary）

| 变量名 | 色值 | 用途 |
|--------|------|------|
| `$color-secondary` | `#1989FA` | 信息提示、辅助操作、TabBar默认态图标 |
| `$color-secondary-light` | `#1989FA` 10%透明度 | 信息类背景色 |

#### 功能色

| 变量名 | 色值 | 用途 |
|--------|------|------|
| `$color-success` | `#07C160` | 成功状态（复用主色） |
| `$color-warning` | `#FF9900` | 警告、进行中状态 |
| `$color-danger` | `#EE0A24` | 错误、危险操作、删除 |
| `$color-info` | `#1989FA` | 信息提示（复用辅助色） |

#### 中性色

| 变量名 | 色值 | 用途 |
|--------|------|------|
| `$color-text-primary` | `#1A1A1A` | 主文本、标题 |
| `$color-text-regular` | `#333333` | 正文文本 |
| `$color-text-secondary` | `#666666` | 辅助说明文字 |
| `$color-text-placeholder` | `#999999` | 占位符、禁用文本 |
| `$color-text-disabled` | `#C0C0C0` | 禁用态文本 |
| `$color-bg-page` | `#F5F5F5` | 页面背景色 |
| `$color-bg-card` | `#FFFFFF` | 卡片背景色 |
| `$color-bg-hover` | `#F7F7F7` | 列表项按压态 |
| `$color-border` | `#E5E5E5` | 分割线、边框 |
| `$color-border-light` | `#F0F0F0` | 浅色分割线 |

#### 预约状态色（复用功能色 + 中性色）

| 状态分组 | 色值 | 对应状态 |
|----------|------|----------|
| 待处理 | `$color-info` `#1989FA` | APPOINTED(1) |
| 进行中 | `$color-warning` `#FF9900` | SIGNED_IN(6), PRECHECK_PASS(7), REGISTERED(8), OBSERVING(10) |
| 异常 | `$color-danger` `#EE0A24` | PRECHECK_FAIL(9) |
| 终态-成功 | `$color-success` `#07C160` | COMPLETED(2) |
| 终态-失活 | `$color-text-placeholder` `#999999` | CANCELLED(3), EXPIRED(4) |

### 字体规范

使用系统默认字体栈，不引入自定义字体文件。

```scss
$font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue',
  'Microsoft YaHei', 'Source Han Sans SC', sans-serif;
```

| 级别 | 字号 | 字重 | 行高 | 用途 |
|------|------|------|------|------|
| `$font-size-xl` | 20px / 40rpx | Medium (500) | 28px | 页面大标题 |
| `$font-size-lg` | 17px / 34rpx | Medium (500) | 24px | 卡片标题、导航标题 |
| `$font-size-base` | 15px / 30rpx | Regular (400) | 21px | 正文、表单标签 |
| `$font-size-sm` | 13px / 26rpx | Regular (400) | 18px | 辅助说明、列表副文本 |
| `$font-size-xs` | 12px / 24rpx | Regular (400) | 16px | 标签、角标、提示文字 |

> 注：rpx 为 uni-app 响应式单位，750rpx = 屏幕宽度。字号同时标注 px 和 rpx，代码中使用 rpx。

### 间距系统

基准单位 4px，使用 4 的倍数保持对齐。

| 变量名 | 值 | 用途 |
|--------|-----|------|
| `$spacing-xs` | 8rpx | 图标与文字间距、紧凑元素内间距 |
| `$spacing-sm` | 16rpx | 列表项内间距、小元素间距 |
| `$spacing-base` | 24rpx | 卡片内间距、段落间距 |
| `$spacing-md` | 32rpx | 模块间距、区块标题与内容间距 |
| `$spacing-lg` | 40rpx | 页面边距（左右padding） |
| `$spacing-xl` | 48rpx | 大模块分隔 |

页面通用布局：左右边距 `$spacing-lg`（40rpx），模块间距 `$spacing-md`（32rpx）。

### 圆角规范

| 变量名 | 值 | 用途 |
|--------|-----|------|
| `$radius-sm` | 8rpx | 小标签、输入框 |
| `$radius-base` | 16rpx | 按钮、小型卡片 |
| `$radius-lg` | 24rpx | 大型卡片、弹窗 |
| `$radius-round` | 50% | 圆形头像、圆形按钮 |
| `$radius-pill` | 999rpx | 胶囊形标签、搜索框 |

### 阴影

仅卡片和浮层使用阴影，保持轻量感。

```scss
$shadow-card: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);           // 卡片默认
$shadow-card-hover: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);     // 卡片按压态
$shadow-float: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);           // 浮层、弹窗
```

### 图标规范

- 使用 uni-ui 内置图标组件 `uni-icons`
- 补充业务图标放入 `static/icons/` 目录，SVG 格式，单色 24x24px
- 图标尺寸统一为 40rpx（列表内）/ 48rpx（独立入口）
- 图标颜色继承父元素 `color` 属性

| 场景 | 图标名 | 说明 |
|------|--------|------|
| TabBar-首页 | `home` | uni-icons 内置 |
| TabBar-预约 | `calendar` | uni-icons 内置 |
| TabBar-我的 | `person` | uni-icons 内置 |
| 添加 | `plusempty` | uni-icons 内置 |
| 搜索 | `search` | uni-icons 内置 |
| 箭头 | `right` / `forward` | uni-icons 内置 |
| 关闭 | `close` | uni-icons 内置 |
| 疫苗 | `static/icons/vaccine.svg` | 自定义：注射器图标 |
| 儿童 | `static/icons/child.svg` | 自定义：儿童轮廓 |
| 状态-成功 | `checkmarkempty` | uni-icons 内置 |
| 状态-警告 | `info` | uni-icons 内置 |
| 状态-错误 | `closeempty` | uni-icons 内置 |

### uni.scss 变量汇总

```scss
// 主色
$color-primary: #07C160;
$color-primary-light: rgba(7, 193, 96, 0.1);
$color-primary-dark: #06AD56;

// 辅助色
$color-secondary: #1989FA;
$color-secondary-light: rgba(25, 137, 250, 0.1);

// 功能色
$color-success: #07C160;
$color-warning: #FF9900;
$color-danger: #EE0A24;
$color-info: #1989FA;

// 中性色-文本
$color-text-primary: #1A1A1A;
$color-text-regular: #333333;
$color-text-secondary: #666666;
$color-text-placeholder: #999999;
$color-text-disabled: #C0C0C0;

// 中性色-背景/边框
$color-bg-page: #F5F5F5;
$color-bg-card: #FFFFFF;
$color-bg-hover: #F7F7F7;
$color-border: #E5E5E5;
$color-border-light: #F0F0F0;

// 字体
$font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue',
  'Microsoft YaHei', 'Source Han Sans SC', sans-serif;
$font-size-xl: 40rpx;
$font-size-lg: 34rpx;
$font-size-base: 30rpx;
$font-size-sm: 26rpx;
$font-size-xs: 24rpx;

// 间距
$spacing-xs: 8rpx;
$spacing-sm: 16rpx;
$spacing-base: 24rpx;
$spacing-md: 32rpx;
$spacing-lg: 40rpx;
$spacing-xl: 48rpx;

// 圆角
$radius-sm: 8rpx;
$radius-base: 16rpx;
$radius-lg: 24rpx;
$radius-round: 50%;
$radius-pill: 999rpx;

// 阴影
$shadow-card: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
$shadow-card-hover: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
$shadow-float: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
```

### TabBar 配置详情

在 `pages.json` 的 `tabBar` 节点中配置，使用原生 TabBar（非自定义）。

```json
{
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#07C160",
    "backgroundColor": "#FFFFFF",
    "borderStyle": "white",
    "height": "50px",
    "fontSize": "10px",
    "iconWidth": "24px",
    "spacing": "3px",
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页",
        "iconPath": "static/tabbar/home.png",
        "selectedIconPath": "static/tabbar/home-active.png"
      },
      {
        "pagePath": "pages/appointment/list",
        "text": "预约",
        "iconPath": "static/tabbar/calendar.png",
        "selectedIconPath": "static/tabbar/calendar-active.png"
      },
      {
        "pagePath": "pages/mine/index",
        "text": "我的",
        "iconPath": "static/tabbar/person.png",
        "selectedIconPath": "static/tabbar/person-active.png"
      }
    ]
  }
}
```

**图标规范：**
- 尺寸：81px × 81px（@3x）
- 格式：PNG，透明背景
- 未选中态：`#999999` 单色
- 选中态：`#07C160` 单色
- 图标风格：线性，线宽 2px，与微信小程序图标风格一致
- 放置目录：`static/tabbar/`

**TabBar 样式变量（供组件内引用）：**

| 变量名 | 值 | 说明 |
|--------|-----|------|
| `$tabbar-color` | `#999999` | 未选中文字/图标颜色 |
| `$tabbar-active-color` | `#07C160` | 选中文字/图标颜色 |
| `$tabbar-bg` | `#FFFFFF` | TabBar背景色 |
| `$tabbar-height` | `50px` | TabBar高度 |

### 按钮系统规范

统一按钮样式，通过 CSS 类名区分类型。

#### 按钮类型

| 类型 | 类名 | 背景色 | 文字色 | 边框 | 用途 |
|------|------|--------|--------|------|------|
| 主要 | `.btn-primary` | `$color-primary` | `#FFFFFF` | 无 | 主要操作：登录、提交、确认 |
| 次要 | `.btn-secondary` | `transparent` | `$color-primary` | 1px solid `$color-primary` | 辅助操作：取消、返回 |
| 危险 | `.btn-danger` | `$color-danger` | `#FFFFFF` | 无 | 危险操作：删除 |
| 文字 | `.btn-text` | `transparent` | `$color-secondary` | 无 | 链接式操作：查看更多、我有疑问 |
| 幽灵 | `.btn-ghost` | `rgba(0,0,0,0.05)` | `$color-text-regular` | 无 | 卡片内操作 |

#### 按钮尺寸

| 尺寸 | 类名 | 高度 | 内边距 | 字号 | 圆角 | 用途 |
|------|------|------|--------|------|------|------|
| 大 | `.btn-lg` | 88rpx | `0 48rpx` | `$font-size-lg` | `$radius-base` | 页面底部固定按钮 |
| 中 | `.btn-md` | 72rpx | `0 32rpx` | `$font-size-base` | `$radius-base` | 表单内按钮 |
| 小 | `.btn-sm` | 56rpx | `0 24rpx` | `$font-size-sm` | `$radius-sm` | 卡片内、行内按钮 |

#### 按钮状态

| 状态 | 视觉表现 |
|------|----------|
| 默认 | 按类型样式 |
| 按压 | 透明度 0.8，过渡 0.15s |
| 禁用 | 透明度 0.4，`pointer-events: none` |
| 加载 | 文字替换为 loading 图标 + "提交中..."，禁用点击 |

#### 按钮布局规则

- **页面底部固定按钮**：距底部 TabBar（如存在）20rpx，距左右 `$spacing-lg`，宽度撑满
- **页面底部双按钮**：左侧次要按钮 + 右侧主要按钮，间距 `$spacing-base`，各占50%
- **表单内按钮**：宽度撑满表单容器
- **行内按钮**：auto宽度，不撑满

#### 按钮示例

```
主要按钮（大）：
┌─────────────────────────────────┐
│              登 录               │  ← 白色文字, 绿色背景
└─────────────────────────────────┘

次要按钮（大）：
┌─────────────────────────────────┐
│              取 消               │  ← 绿色文字, 透明背景, 绿色边框
└─────────────────────────────────┘

双按钮布局：
┌────────────────┐ ┌────────────────┐
│     取 消       │ │     确 认       │
└────────────────┘ └────────────────┘
  次要按钮              主要按钮

危险按钮（中）：
┌─────────────────────────────────┐
│            删 除                │  ← 白色文字, 红色背景
└─────────────────────────────────┘
```

## 组件设计规范

### 组件分类

| 分类 | 说明 | 放置目录 |
|------|------|----------|
| 业务组件 | 包含业务逻辑和数据展示 | `components/` |
| 布局组件 | 纯结构、无业务逻辑 | `components/` |
| uni-ui组件 | 通用UI组件，直接引用 | `uni_modules/` |

### 业务组件清单

#### 1. `AppStatusTag` — 状态标签

根据预约状态码显示对应颜色的标签。

**Props:**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Number | 是 | 预约状态码（1-10） |

**样式规则：**

- 尺寸：auto宽度，高度 40rpx，内边距 `0 $spacing-sm`
- 圆角：`$radius-pill`
- 字号：`$font-size-xs`，字重 Medium
- 背景色：状态对应功能色 10% 透明度
- 文字色：状态对应功能色
- 对应关系见"预约状态色"表

**示例：**
```
┌──────────┐
│  待签到   │  蓝色标签
└──────────┘
┌──────────┐
│  留观中   │  橙色标签
└──────────┘
```

#### 2. `AppointmentCard` — 预约卡片

预约列表和首页中的预约摘要卡片。

**Props:**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appointment | Object | 是 | 预约对象 |
| showAction | Boolean | 否 | 是否显示操作按钮，默认 false |

**Events:**

| 事件 | 参数 | 说明 |
|------|------|------|
| click | appointment | 点击卡片 |
| cancel | appointmentId | 取消预约 |

**布局：**
```
┌──────────────────────────────────┐
│  疫苗名称              [状态标签] │  ← $font-size-lg + $font-size-sm
│  ─────────────────────────────── │  ← $color-border-light 分割线
│  👶 儿童姓名    📅 2026-04-05    │  ← $font-size-sm, $color-text-secondary
│  🕐 上午(08:00-12:00)           │  ← $font-size-sm, $color-text-secondary
└──────────────────────────────────┘
```

**样式规则：**
- 背景：`$color-bg-card`
- 圆角：`$radius-lg`
- 内边距：`$spacing-base`
- 阴影：`$shadow-card`
- 点击态：`$shadow-card-hover` + `scale(0.99)` 过渡 0.15s
- 卡片间距：`$spacing-base`

#### 3. `ChildCard` — 儿童信息卡

儿童列表中的儿童摘要卡片。

**Props:**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| child | Object | 是 | 儿童对象 |
| selectable | Boolean | 否 | 是否可选（创建预约时），默认 false |
| selected | Boolean | 否 | 是否已选中，默认 false |
| showDelete | Boolean | 否 | 是否显示删除入口，默认 false |

**Events:**

| 事件 | 参数 | 说明 |
|------|------|------|
| click | child | 点击卡片 |
| delete | childId | 删除儿童 |

**布局：**
```
┌──────────────────────────────────┐
│  ┌────┐                          │
│  │头像│  张小明                   │  ← $font-size-lg + $font-size-base
│  │ 图 │  男 · 2岁3个月            │  ← $font-size-sm, $color-text-secondary
│  │ 标 │  320***********1234       │  ← $font-size-xs, $color-text-placeholder
│  └────┘                    [>]   │
└──────────────────────────────────┘
```

**样式规则：**
- 头像：80rpx × 80rpx 圆形，性别图标背景色（男 `$color-info` / 女 `$color-danger`）
- 身份证号脱敏：显示前3后4位，中间用 `*` 替代
- 可选模式：右侧显示 Radio 单选按钮替代箭头

#### 4. `VaccineCard` — 疫苗信息卡

疫苗目录和创建预约中的疫苗卡片。

**Props:**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| vaccine | Object | 是 | 疫苗对象 |
| selectable | Boolean | 否 | 是否可选，默认 false |
| selected | Boolean | 否 | 是否已选中，默认 false |
| showStock | Boolean | 否 | 是否显示库存，默认 false |

**Events:**

| 事件 | 参数 | 说明 |
|------|------|------|
| click | vaccine | 点击卡片 |

**布局：**
```
┌──────────────────────────────────┐
│  疫苗名称            [一类疫苗]  │  ← $font-size-lg + 类型标签
│  生产厂家：XX生物制品有限公司      │  ← $font-size-sm, $color-text-secondary
│  适应年龄：6月龄-12月龄           │
│  接种剂次：共3剂，间隔28天         │
│  ┌──────────────────────────┐    │
│  │ 简介：预防乙型肝炎病毒... │    │  ← $font-size-xs, 最多2行省略
│  └──────────────────────────┘    │
│                    库存: 120  [>] │  ← 可选模式下显示Radio
└──────────────────────────────────┘
```

**样式规则：**
- 类型标签：一类疫苗绿色标签 `CLASS_I`，二类疫苗蓝色标签 `CLASS_II`
- 简介区域：`$color-bg-page` 背景，`$radius-sm` 圆角
- 库存不足（< 10）时数字显示红色

#### 5. `NoticeCard` — 公告卡片

公告列表中的公告摘要卡片。

**Props:**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| notice | Object | 是 | 公告对象 |

**Events:**

| 事件 | 参数 | 说明 |
|------|------|------|
| click | notice | 点击卡片 |

**布局：**
```
┌──────────────────────────────────┐
│  [公告]  关于XX疫苗到货通知        │  ← 分类标签 + 标题
│         2026-04-03               │  ← $font-size-xs, $color-text-placeholder
└──────────────────────────────────┘
```

**样式规则：**
- 无圆角、无阴影（列表项样式）
- 分割线分隔，`$color-border-light`
- 标题单行省略
- 点击态：`$color-bg-hover`

#### 6. `StepIndicator` — 步骤指示器

创建预约的3步骤条和流程指引的6步骤条。

**Props:**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| steps | Array | 是 | 步骤数组 `[{title, description}]` |
| current | Number | 是 | 当前步骤索引（0-based） |

**布局（3步式 - 创建预约）：**
```
  ①            ②            ③
选儿童  ───  选疫苗  ───  选时间
         ✓            →
```

**布局（6步式 - 流程指引）：**
```
  签到  →  预检  →  登记  →  接种  →  留观  →  完成
  [✓]    [✓]    [●]    [○]    [○]    [○]
```

**样式规则：**
- 已完成步骤：`$color-success` 图标（勾）+ 文字
- 当前步骤：`$color-primary` 圆点 + 文字（加粗）
- 未完成步骤：`$color-text-placeholder` 圆点 + 文字
- 连接线：已完成 `$color-success`，未完成 `$color-border`
- 步骤间距：等分容器宽度
- 6步式横向可滚动

#### 7. `QueueInfoCard` — 排队信息卡

流程指引页中显示当前窗口排队信息。

**Props:**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| windowName | String | 是 | 窗口名称 |
| queuePosition | Number | 是 | 当前排队位置 |
| estimatedWait | Number | 否 | 预计等待分钟数 |
| isCalling | Boolean | 否 | 是否正在叫号，默认 false |

**布局：**
```
┌──────────────────────────────────┐
│  📍 签到窗口（1号窗口）           │  ← 窗口名称
│                                  │
│  排队位置                        │
│       3                         │  ← $font-size-xl (80rpx), $color-primary
│                                  │
│  前方还有 2 人                   │  ← $font-size-sm, $color-text-secondary
│  预计等待约 10 分钟               │
│                                  │
│  ⏱ 上次刷新: 14:30:05            │  ← $font-size-xs, $color-text-placeholder
└──────────────────────────────────┘
```

**样式规则：**
- 背景色：`$color-primary-light`（主色浅色背景）
- 排队数字居中，`$font-size-xl`，`$color-primary`
- 叫号中状态：卡片边框闪烁动画（`$color-warning` 2px solid）

#### 8. `EmptyState` — 空状态

通用空数据占位组件。

**Props:**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| icon | String | 否 | uni-icons 图标名，默认 `empty` |
| title | String | 否 | 提示标题，默认"暂无数据" |
| description | String | 否 | 补充说明 |
| actionText | String | 否 | 操作按钮文字 |

**Events:**

| 事件 | 参数 | 说明 |
|------|------|------|
| action | — | 点击操作按钮 |

**布局：**
```
│                                  │
│         ┌─────────┐              │
│         │  📭     │              │  ← 图标 120rpx, $color-text-placeholder
│         └─────────┘              │
│                                  │
│         暂无预约记录              │  ← $font-size-base, $color-text-secondary
│    快去预约疫苗接种吧             │  ← $font-size-sm, $color-text-placeholder
│                                  │
│       [ 立即预约 ]               │  ← 可选操作按钮, $color-primary
│                                  │
```

#### 9. `UserInfoCard` — 用户信息卡片

个人中心页顶部用户信息展示。

**Props:**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userInfo | Object | 是 | 用户信息对象 |

**布局：**
```
┌──────────────────────────────────┐
│  ┌────┐                          │
│  │头像│  张三                     │
│  │ 图 │  138****0001              │
│  │ 标 │  账号状态正常              │  ← 绿色文字
│  └────┘                          │
└──────────────────────────────────┘
```

**样式规则：**
- 头像：120rpx × 120rpx 圆形，默认灰色背景 + 姓氏首字
- 手机号脱敏：显示前3后4位
- 背景：`$color-bg-card`
- 圆角：`$radius-lg`
- 内边距：`$spacing-lg`（上下 `$spacing-xl`）

### uni-ui 组件使用规范

| 场景 | 组件 | 说明 |
|------|------|------|
| 导航栏 | `uni-nav-bar` | 自定义导航栏，标题居中 |
| TabBar | 原生 TabBar | `pages.json` 中配置，不用自定义 |
| 弹窗 | `uni-popup` | 底部弹出/居中弹窗 |
| 加载 | `uni-load-more` | 列表加载更多/加载完毕 |
| 表单 | `uni-forms` + `uni-easyinput` + `uni-data-picker` | 表单验证与输入 |
| 日期选择 | `uni-datetime-picker` | 日期/时间选择 |
| 标签页 | `uni-segmented-control` | 页面内Tab切换 |
| 轮播 | `uni-swiper-dot` | 公告轮播指示器 |
| 操作菜单 | `uni-list` + `uni-list-item` | 个人中心菜单列表 |
| 消息提示 | `uni.showToast` / `uni.showModal` | 全局提示/确认弹窗 |
| 骨架屏 | `uni-skeleton` | 页面加载骨架 |

## 详细页面布局设计

> 以下使用 ASCII 线框图描述各页面布局。所有页面遵循统一规范：
> - 页面背景：`$color-bg-page`
> - 内容区域左右边距：`$spacing-lg`（40rpx）
> - 模块间距：`$spacing-md`（32rpx）
> - 导航栏使用 `uni-nav-bar`，高度 44px + 状态栏高度

### 登录页 (`pages/auth/login`)

```
┌─────────────────────────────────┐
│         [导航栏 - 空白]         │  ← 无标题，沉浸式
│                                 │
│                                 │
│         ┌─────────┐             │
│         │  LOGO   │             │  ← 160rpx, 品牌图标
│         └─────────┘             │
│        疫苗管理系统              │  ← $font-size-xl, $color-text-primary
│       家长端                     │  ← $font-size-sm, $color-text-secondary
│                                 │
│  ┌─────────────────────────┐    │
│  │ 📱 请输入手机号           │    │  ← uni-easyinput, $radius-base
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │ 🔒 请输入密码             │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─ 密码登录 ─┬─ 验证码登录 ─┐  │  ← uni-segmented-control
│  └─────────────────────────────┘  ← 验证码Tab显示: 短信输入框 + 获取按钮
│                                 │
│  ┌─────────────────────────┐    │
│  │         登 录            │    │  ← 主色按钮, 高度88rpx, $radius-base
│  └─────────────────────────┘    │
│                                 │
│     忘记密码？  立即注册 >       │  ← $font-size-sm, $color-secondary
│                                 │
└─────────────────────────────────┘
```

**布局说明：**
- 页面垂直居中排列，LOGO + 标题 + 表单
- 登录方式通过 SegmentedControl 切换，验证码Tab额外显示短信输入框和60秒倒计时按钮
- 倒计时按钮：`$color-secondary` 文字色，禁用时 `$color-text-disabled`

### 注册页 (`pages/auth/register`)

```
┌─────────────────────────────────┐
│  <  注册                   [×]  │  ← uni-nav-bar, 返回+关闭
│                                 │
│  手机号 *                       │  ← $font-size-base, 必填标记红色
│  ┌────────────────────┐ ┌────┐  │
│  │ 请输入手机号         │ │获取│  │  ← 输入框 + 获取验证码按钮
│  └────────────────────┘ └────┘  │
│                                 │
│  验证码 *                       │
│  ┌─────────────────────────┐    │
│  │ 请输入6位验证码           │    │
│  └─────────────────────────┘    │
│                                 │
│  密码 *                         │
│  ┌─────────────────────────┐    │
│  │ 请输入6-20位密码          │    │  ← 提示: 包含字母和数字
│  └─────────────────────────┘    │
│                                 │
│  确认密码 *                     │
│  ┌─────────────────────────┐    │
│  │ 请再次输入密码            │    │
│  └─────────────────────────┘    │
│                                 │
│  真实姓名 *                     │
│  ┌─────────────────────────┐    │
│  │ 请输入真实姓名            │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │         注 册            │    │
│  └─────────────────────────┘    │
│                                 │
│  已有账号？立即登录 >            │
│                                 │
│  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐  │
│  │  注册须知：               │  │  ← $color-bg-page, $radius-base
│  │  · 一个手机号只能注册一次  │  │
│  │  · 密码需包含字母和数字    │  │
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘  │
└─────────────────────────────────┘
```

**布局说明：**
- 表单字段垂直排列，标签在上、输入框在下
- 必填字段标签后带红色 `*` 标记
- 注册须知区域使用浅灰背景区分

### 首页 (`pages/index/index`)

```
┌─────────────────────────────────┐
│         疫苗管理系统             │  ← 自定义导航栏
│                                 │
│  您好，张三                      │  ← $font-size-lg
│  2026年4月4日 星期六              │  ← $font-size-sm, $color-text-secondary
│                                 │
│  ┌─────────────────────────┐    │
│  │ ┌───┐  ┌───┐  ┌───┐    │    │  ← uni-swiper, 公告轮播
│  │ │公告1│→│公告2│→│公告3│   │    │     高度 200rpx, $radius-lg
│  │ └───┘  └───┘  └───┘    │    │
│  │         ● ○ ○            │    │  ← 轮播指示器
│  └─────────────────────────┘    │
│                                 │
│  今日预约                        │  ← 模块标题, $font-size-lg
│  ┌─────────────────────────┐    │
│  │  乙肝疫苗（第1剂）        │    │  ← AppointmentCard
│  │  👶 张小明  📅 04-05     │    │
│  │  🕐 上午          [待签到]│    │
│  └─────────────────────────┘    │
│  ─ 或 ─                         │  ← 无预约时显示 EmptyState
│  ┌─────────────────────────┐    │
│  │     📭 暂无今日预约      │    │
│  │   [立即预约]             │    │
│  └─────────────────────────┘    │
│                                 │
│  快捷服务                        │  ← 模块标题
│  ┌────────┐  ┌────────┐        │
│  │  💉    │  │  👶    │        │  ← 2x2 网格, $color-bg-card
│  │ 预约接种│  │ 儿童管理│        │     图标48rpx, 间距$spacing-sm
│  └────────┘  └────────┘        │     $radius-lg, $shadow-card
│  ┌────────┐  ┌────────┐        │
│  │  📋    │  │  📢    │        │
│  │ 疫苗目录│  │ 系统公告│        │
│  └────────┘  └────────┘        │
│                                 │
│  ─────────────────────────────  │
│  [  🏠 首页  |  📅 预约  | 👤  ]│  ← 原生 TabBar
└─────────────────────────────────┘
```

**布局说明：**
- 页面可滚动，内容超出时向下滚动
- 公告轮播：横向滚动，3秒自动切换，点击跳转公告详情
- 快捷服务网格：等宽2列，每个入口高度 160rpx
- 今日预约区域：有预约显示 AppointmentCard，无预约显示 EmptyState

### 创建预约 (`pages/appointment/create`)

```
┌─────────────────────────────────┐
│  <  预约接种                     │  ← uni-nav-bar
│                                 │
│  ┌─────────────────────────┐    │
│  │  ① 选儿童  ② 选疫苗  ③ 选时间│  ← StepIndicator(3步)
│  └─────────────────────────┘    │
│                                 │
│  ── 步骤1: 选择儿童 ──           │
│                                 │
│  ┌─────────────────────────┐    │
│  │ (•) 张小明               │    │  ← ChildCard, selectable=true
│  │     男 · 2岁3个月         │    │     Radio 选中态
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │ ( ) 张小红               │    │  ← ChildCard, 未选中
│  │     女 · 5岁1个月         │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐  │
│  │     + 添加儿童            │    │  ← 添加入口, 虚线边框
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘  │
│                                 │
│  ┌─────────────────────────┐    │
│  │         下一步           │    │  ← 底部固定按钮
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

**步骤2: 选择疫苗**

```
│  ── 步骤2: 选择疫苗 ──           │
│                                 │
│  ┌─────────────────────────┐    │
│  │ 🔍 搜索疫苗名称...       │    │  ← 搜索框, $radius-pill
│  └─────────────────────────┘    │
│                                 │
│  [全部] [一类疫苗] [二类疫苗]    │  ← uni-segmented-control
│                                 │
│  一类疫苗                        │  ← 分组标题
│  ┌─────────────────────────┐    │
│  │ (•) 乙肝疫苗  [一类]     │    │  ← VaccineCard, selectable
│  │     XX生物 · 6月龄起     │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │ ( ) 卡介苗    [一类]     │    │
│  │     XX生物 · 出生时      │    │
│  └─────────────────────────┘    │
│                                 │
│  二类疫苗                        │
│  ┌─────────────────────────┐    │
│  │ ( ) 手足口疫苗 [二类]    │    │
│  │     XX生物 · 6月龄起     │    │
│  └─────────────────────────┘    │
```

**步骤3: 选择时间**

```
│  ── 步骤3: 选择时间 ──           │
│                                 │
│  ┌─────────────────────────┐    │
│  │ 📋 乙肝疫苗（第1剂）      │    │  ← 已选疫苗摘要, $color-bg-page
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │    2026年4月              │    │  ← uni-datetime-picker 日历
│  │ 日 一 二 三 四 五 六      │    │     仅未来7天可选
│  │        1  2  3  4        │    │     今天及之前日期置灰
│  │  5  6  7  8  9 10 11     │    │     周末可标记不同颜色
│  └─────────────────────────┘    │
│                                 │
│  选择时段                        │
│  ┌────────────────┐ ┌────────┐ │
│  │ (•) 上午        │ │ ( ) 下午│ │  ← Radio选择
│  │ 08:00 - 12:00  │ │14:00-17│ │
│  │ 剩余: 15        │ │ 剩余: 8│ │  ← 显示剩余名额
│  └────────────────┘ └────────┘ │
│                                 │
│  ┌─────────────────────────┐    │
│  │       确认预约            │    │  ← 底部固定, $color-primary
│  └─────────────────────────┘    │
```

### 预约列表 (`pages/appointment/list`)

```
┌─────────────────────────────────┐
│         我的预约                  │  ← 导航栏
│                                 │
│  [全部][待签到][进行中][已完成]   │  ← uni-segmented-control
│  [已取消]                        │     支持横向滚动
│                                 │
│  ┌─────────────────────────┐    │
│  │  乙肝疫苗（第1剂） [待签到]│    │  ← AppointmentCard
│  │  👶 张小明  📅 04-05     │    │
│  │  🕐 上午              [>]│    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │  卡介苗      [已完成]     │    │
│  │  👶 张小红  📅 03-20     │    │
│  │  🕐 下午              [>]│    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │  百白破      [已取消]     │    │
│  │  👶 张小明  📅 03-15     │    │
│  │  🕐 上午              [>]│    │
│  └─────────────────────────┘    │
│                                 │
│  ── 下拉刷新 / 上拉加载更多 ──   │
│                                 │
│  ┌─────────────────────────┐    │
│  │     📭 暂无预约记录       │    │  ← EmptyState
│  └─────────────────────────┘    │
│                                 │
│  ┌──┐                           │
│  │ +│                           │  ← 浮动按钮, 右下角
│  └──┘                           │     $color-primary, 100rpx圆形
│                                 │
│  ─────────────────────────────  │
│  [  🏠 首页  |  📅 预约  | 👤  ]│
└─────────────────────────────────┘
```

**布局说明：**
- Tab 筛选栏使用 `uni-segmented-control`，宽度超出时横向滚动
- 列表按预约日期降序排列
- 浮动按钮位置：距底部 TabBar 40rpx，距右侧 40rpx
- 下拉刷新使用页面原生 `onPullDownRefresh`，上拉使用 `uni-load-more`

### 预约详情 (`pages/appointment/detail`)

```
┌─────────────────────────────────┐
│  <  预约详情                     │
│                                 │
│  ┌─────────────────────────┐    │
│  │       ● 待签到            │    │  ← 状态卡片, 背景色=状态色10%
│  │    乙肝疫苗（第1剂）       │    │     状态文字=状态色, $font-size-xl
│  └─────────────────────────┘    │
│                                 │
│  预约信息                        │  ← 模块标题
│  ┌─────────────────────────┐    │
│  │  预约编号  YY20260405001 │    │  ← 键值对列表
│  │  接种日期  2026-04-05    │    │     标签: $color-text-secondary
│  │  接种时段  上午           │    │     值: $color-text-primary
│  └─────────────────────────┘    │
│                                 │
│  儿童信息                        │
│  ┌─────────────────────────┐    │
│  │  姓  名  张小明           │    │
│  │  性  别  男               │    │
│  │  出生日期 2023-12-15     │    │
│  └─────────────────────────┘    │
│                                 │
│  疫苗信息                        │
│  ┌─────────────────────────┐    │
│  │  疫苗名称 乙肝疫苗       │    │
│  │  疫苗类型 一类疫苗       │    │
│  │  生产厂家 XX生物         │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │     [取消预约]            │    │  ← 仅 status=1 时显示
│  │     [查看流程]            │    │  ← status=6,7,8,10 时显示
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

### 流程指引 (`pages/appointment/guide`)

```
┌─────────────────────────────────┐
│  <  接种流程                     │
│                                 │
│  ┌─────────────────────────┐    │
│  │ 签到→预检→登记→接种→留观→完成│  ← StepIndicator(6步)
│  │ [✓]  [✓]  [●]  [○]  [○] [○]│     current=2 (已登记)
│  └─────────────────────────┘    │
│                                 │
│  当前步骤：登记                   │  ← $font-size-base, $color-primary
│  请前往3号窗口完成疫苗接种登记    │  ← $font-size-sm, $color-text-secondary
│                                 │
│  排队信息                        │
│  ┌─────────────────────────┐    │
│  │  📍 登记窗口（3号窗口）    │    │  ← QueueInfoCard
│  │                          │    │
│  │       3                  │    │
│  │                          │    │
│  │  前方还有 2 人           │    │
│  │  预计等待约 10 分钟       │    │
│  │                          │    │
│  │  ⏱ 14:30:05 自动刷新    │    │
│  └─────────────────────────┘    │
│                                 │
│  温馨提示                        │
│  ┌─────────────────────────┐    │
│  │  · 请保持手机畅通，注意叫号│    │  ← $color-bg-page, $radius-base
│  │  · 请携带儿童预防接种证   │    │     $font-size-sm
│  │  · 过号需重新排队         │    │
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

**布局说明：**
- 页面每30秒自动刷新排队信息（WebSocket 不可用时降级为轮询）
- 轮询生命周期：页面 `onShow` 时启动30秒定时器调用排队信息API，`onHide`/`onUnload` 时清除定时器，避免页面不可见时继续发送无效请求
- 轮询冲突处理：每次启动轮询前先清除已有定时器（`clearInterval`），避免页面快速切换时产生多个并行轮询；轮询回调中检查页面是否仍可见（`getCurrentPages()` 判断），不可见时自动停止
- 叫号中时 QueueInfoCard 边框闪烁
- 步骤指示器可横向滚动（6步在小屏可能溢出）

### 儿童列表 (`pages/child/list`)

```
┌─────────────────────────────────┐
│  儿童管理                    [+] │  ← 导航栏, 右侧添加按钮
│                                 │
│  共2位儿童（最多5位）             │  ← $font-size-sm, $color-text-secondary
│                                 │
│  ┌─────────────────────────┐    │
│  │ ┌────┐                  │    │
│  │ │ 张 │  张小明        [>]│    │  ← ChildCard, showDelete=true
│  │ └────┘  男 · 2岁3个月    │    │     支持左滑显示删除按钮
│  │         320***1234       │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │ ┌────┐                  │    │
│  │ │ 张 │  张小红        [>]│    │
│  │ └────┘  女 · 5岁1个月    │    │
│  │         320***5678       │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐  │
│  │     📭 暂无儿童信息        │  │  ← EmptyState
│  │     [添加第一位儿童]       │  │
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘  │
│                                 │
│  ┌─────────────────────────┐    │
│  │     + 添加儿童            │    │  ← 底部固定按钮（列表非空时）
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

### 添加/编辑儿童 (`pages/child/add`)

```
┌─────────────────────────────────┐
│  <  添加儿童          [保存]     │  ← 编辑模式显示"编辑儿童"+保存按钮
│                                 │
│  基本信息                        │  ← 分组标题
│  ┌─────────────────────────┐    │
│  │  姓名 *                 │    │
│  │  ┌─────────────────┐    │    │
│  │  │ 请输入儿童姓名    │    │    │  ← uni-easyinput
│  │  └─────────────────┘    │    │
│  │                         │    │
│  │  性别 *                 │    │
│  │  ┌──────┐ ┌──────┐    │    │
│  │  │ 男 ● │ │ 女 ○ │    │    │  ← Radio 单选
│  │  └──────┘ └──────┘    │    │
│  │                         │    │
│  │  出生日期 *              │    │
│  │  ┌─────────────────┐    │    │
│  │  │ 2023-12-15   [📅]│    │    │  ← uni-datetime-picker
│  │  └─────────────────┘    │    │     不可超过今天
│  └─────────────────────────┘    │
│                                 │
│  证件信息                        │
│  ┌─────────────────────────┐    │
│  │  证件类型                │    │
│  │  ┌─────────────────┐    │    │
│  │  │ 居民身份证   [▼] │    │    │  ← uni-data-picker
│  │  └─────────────────┘    │    │
│  │                         │    │
│  │  证件号码                │    │
│  │  ┌─────────────────┐    │    │
│  │  │ 请输入证件号码    │    │    │
│  │  └─────────────────┘    │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │         保存             │    │  ← 底部固定按钮
│  └─────────────────────────┘    │
│                                 │
│  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐  │
│  │  提示：                   │  │
│  │  · 带 * 为必填项          │  │
│  │  · 证件号码用于接种核验    │  │
│  │  · 出生日期和证件号创建后  │  │
│  │    不可修改               │  │
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘  │
│                                 │
└─────────────────────────────────┘
```

### 疫苗目录 (`pages/vaccine/list`)

```
┌─────────────────────────────────┐
│         疫苗目录                  │
│                                 │
│  ┌─────────────────────────┐    │
│  │ 🔍 搜索疫苗名称...       │    │  ← 搜索框, $radius-pill
│  └─────────────────────────┘    │
│                                 │
│  [全部] [一类疫苗] [二类疫苗]    │  ← uni-segmented-control
│                                 │
│  一类疫苗（免费）                 │  ← 分组标题
│  ┌─────────────────────────┐    │
│  │  乙肝疫苗      [一类]    │    │  ← VaccineCard
│  │  XX生物制品有限公司      │    │
│  │  适应年龄：出生时         │    │
│  │  ┌──────────────────┐   │    │
│  │  │ 预防乙型肝炎...  │   │    │  ← 简介2行省略
│  │  └──────────────────┘   │    │
│  │                     [>] │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │  卡介苗        [一类]    │    │
│  │  XX生物制品有限公司      │    │
│  │  ...                     │    │
│  └─────────────────────────┘    │
│                                 │
│  二类疫苗（自费）                 │
│  ┌─────────────────────────┐    │
│  │  手足口疫苗    [二类]    │    │
│  │  XX生物制品有限公司      │    │
│  │  ¥ 198.00               │    │  ← 二类疫苗显示价格
│  │  ...                     │    │
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

### 疫苗详情 (`pages/vaccine/detail`)

```
┌─────────────────────────────────┐
│  <  疫苗详情                     │
│                                 │
│  乙肝疫苗                        │  ← $font-size-xl
│  ┌────────┐                     │
│  │一类疫苗│                      │  ← 类型标签
│  └────────┘                     │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  基本信息                        │  ← 模块标题
│  ┌─────────────────────────┐    │
│  │  生产厂家  XX生物        │    │
│  │  规格      0.5ml/支      │    │
│  │  接种剂次  共3剂          │    │
│  │  间隔天数  28天           │    │
│  │  适应年龄  6月龄-12月龄   │    │
│  │  参考价格  免费            │    │  ← 二类显示价格
│  └─────────────────────────┘    │
│                                 │
│  疫苗说明                        │
│  ┌─────────────────────────┐    │
│  │  本疫苗用于预防乙型肝炎   │    │  ← 完整描述文本
│  │  病毒感染，适用于6月龄    │    │     $font-size-base
│  │  至12月龄婴幼儿...        │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │     [立即预约]            │    │  ← 底部固定按钮
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

### 个人中心 (`pages/mine/index`)

```
┌─────────────────────────────────┐
│         个人中心                  │
│                                 │
│  ┌─────────────────────────┐    │
│  │ ┌────┐                  │    │  ← UserInfoCard
│  │ │ 张 │  张三             │    │
│  │ └────┘  138****0001      │    │
│  │         账号状态正常       │    │  ← 绿色文字
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │  👶 儿童管理          [>]│    │  ← uni-list-item
│  ├─────────────────────────┤    │
│  │  📋 接种记录          [>]│    │
│  ├─────────────────────────┤    │
│  │  🔒 修改密码          [>]│    │
│  ├─────────────────────────┤    │
│  │  ℹ️  关于系统          [>]│    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │       退出登录            │    │  ← $color-text-secondary 边框按钮
│  └─────────────────────────┘    │
│                                 │
│  ─────────────────────────────  │
│  [  🏠 首页  |  📅 预约  | 👤  ]│
└─────────────────────────────────┘
```

### 公告列表 (`pages/notice/list`)

```
┌─────────────────────────────────┐
│         系统公告                  │
│                                 │
│  ┌─────────────────────────┐    │
│  │ [公告] 关于XX疫苗到货...  │    │  ← NoticeCard
│  │        2026-04-03    [>] │    │
│  ├─────────────────────────┤    │
│  │ [通知] 接种时间调整通知   │    │
│  │        2026-04-01    [>] │    │
│  ├─────────────────────────┤    │
│  │ [公告] 春季接种注意事项   │    │
│  │        2026-03-28    [>] │    │
│  └─────────────────────────┘    │
│                                 │
│  ── 上拉加载更多 ──              │
│                                 │
└─────────────────────────────────┘
```

### 公告详情 (`pages/notice/detail`)

```
┌─────────────────────────────────┐
│  <  公告详情                     │
│                                 │
│  关于XX疫苗到货通知               │  ← $font-size-xl
│  2026-04-03                      │  ← $font-size-xs, $color-text-placeholder
│  ─────────────────────────────  │
│                                 │
│  尊敬的各位家长：                  │
│                                 │
│  我中心已到货XX疫苗（批次号：     │  ← 正文, $font-size-base
│  XXXXX），欢迎各位家长为儿童      │     行高 1.8
│  预约接种...                     │
│                                 │
│                                 │
│                                 │
│  ┌─────────────────────────┐    │
│  │       我有疑问            │    │  ← 底部固定, 次要按钮样式
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

### 忘记密码 (`pages/auth/forgot-password`)

```
┌─────────────────────────────────┐
│  <  忘记密码                     │
│                                 │
│  请输入注册时使用的手机号码        │  ← $font-size-sm, $color-text-secondary
│                                 │
│  手机号 *                       │
│  ┌────────────────────┐ ┌────┐  │
│  │ 请输入手机号         │ │获取│  │  ← 输入框 + 获取验证码
│  └────────────────────┘ └────┘  │
│                                 │
│  验证码 *                       │
│  ┌─────────────────────────┐    │
│  │ 请输入6位验证码           │    │
│  └─────────────────────────┘    │
│                                 │
│  新密码 *                       │
│  ┌─────────────────────────┐    │
│  │ 请输入新密码              │    │  ← 提示: 6-20位，包含字母和数字
│  └─────────────────────────┘    │
│                                 │
│  确认新密码 *                   │
│  ┌─────────────────────────┐    │
│  │ 请再次输入新密码          │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │        重置密码           │    │  ← $color-primary, 底部固定
│  └─────────────────────────┘    │
│                                 │
│  ── 想起密码了？立即登录 > ──    │
│                                 │
└─────────────────────────────────┘
```

**布局说明：**
- 调用 `POST /api/v1/user/password/reset`，请求体：`{ phone, smsCode, newPassword }`
- 重置成功 → toast "密码重置成功" → 跳转登录页
- 表单校验规则同注册页（手机号、验证码、密码）

### 修改密码 (`pages/auth/change-password`)

```
┌─────────────────────────────────┐
│  <  修改密码                     │
│                                 │
│  当前密码 *                     │
│  ┌─────────────────────────┐    │
│  │ 请输入当前密码            │    │
│  └─────────────────────────┘    │
│                                 │
│  新密码 *                       │
│  ┌─────────────────────────┐    │
│  │ 请输入新密码              │    │  ← 提示: 6-20位，包含字母和数字
│  └─────────────────────────┘    │
│                                 │
│  确认新密码 *                   │
│  ┌─────────────────────────┐    │
│  │ 请再次输入新密码          │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │        确认修改           │    │  ← $color-primary, 底部固定
│  └─────────────────────────┘    │
│                                 │
│  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐  │
│  │  密码修改成功后需要重新登录  │  │  ← 提示区域
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘  │
│                                 │
└─────────────────────────────────┘
```

**布局说明：**
- 调用 `PUT /api/v1/user/password`，请求体：`{ oldPassword, newPassword }`
- 修改成功 → 清除token → 跳转登录页（需重新登录）
- 新密码不可与当前密码相同

### 公告反馈 (`pages/notice/feedback`)

```
┌─────────────────────────────────┐
│  <  公告反馈                     │
│                                 │
│  关于XX疫苗到货通知               │  ← 公告标题（只读展示）
│  2026-04-03                      │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  您的反馈 *                     │
│  ┌─────────────────────────┐    │
│  │                         │    │
│  │  请输入您的反馈内容...    │    │  ← textarea, 最少10字
│  │                         │    │     最多500字
│  │                         │    │     右下角显示字数: 0/500
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │        提交反馈           │    │  ← $color-primary, 底部固定
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

**布局说明：**
- 从公告详情页跳转，URL参数携带 `noticeId` 和公告标题
- 调用 `POST /api/v1/public/notices/{noticeId}/feedback`，请求体：`{ content }`
- 提交成功 → toast "反馈提交成功" → 返回公告详情页
- 表单校验：`content` 必填，10-500字

### 接种记录 (`pages/record/list`)

```
┌─────────────────────────────────┐
│  <  接种记录                     │
│                                 │
│  ┌─────────────────────────┐    │
│  │ 请选择儿童         [▼] │    │  ← 儿童筛选下拉
│  └─────────────────────────┘    │     默认"全部"，有多个儿童时可筛选
│                                 │
│  共接种 5 剂                    │  ← $font-size-sm, $color-text-secondary
│                                 │
│  ── 2026年4月 ──                │  ← 按月分组标题
│                                 │
│  ┌─────────────────────────┐    │
│  │  乙肝疫苗（第1剂）        │    │  ← 接种记录卡片
│  │  👶 张小明               │    │
│  │  📅 2026-04-05  上午     │    │
│  │  🏥 XX社区卫生服务中心    │    │  ← $font-size-xs, $color-text-secondary
│  │  📋 批次: B20260405001   │    │
│  │              [已完成 ✓]  │    │  ← 绿色标签
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │  卡介苗（第1剂）          │    │
│  │  👶 张小明               │    │
│  │  📅 2026-03-20  下午     │    │
│  │  🏥 XX社区卫生服务中心    │    │
│  │  📋 批次: B20260320003   │    │
│  │              [已完成 ✓]  │    │
│  └─────────────────────────┘    │
│                                 │
│  ── 2025年12月 ──               │
│                                 │
│  ┌─────────────────────────┐    │
│  │  乙肝疫苗（第0剂）        │    │
│  │  👶 张小明               │    │
│  │  📅 2025-12-15  上午     │    │
│  │  ...                     │    │
│  └─────────────────────────┘    │
│                                 │
│  ── 上拉加载更多 ──              │
│                                 │
│  ┌─────────────────────────┐    │
│  │     📭 暂无接种记录       │    │  ← EmptyState
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

**布局说明：**
- 调用 `GET /api/v1/user/records` 或 `GET /api/v1/user/records/child/{childId}`
- 儿童筛选：顶部下拉选择器，仅有1个儿童时默认选中不显示选择器
- 记录按接种日期降序排列，按月分组显示
- 每条记录展示：疫苗名称（含剂次）、儿童姓名、接种日期时段、接种单位、批次号、状态
- 支持上拉加载更多
- 从个人中心"接种记录"菜单或首页"接种记录"快捷入口进入

**接种记录卡片组件 `RecordCard`：**

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| record | Object | 是 | 接种记录对象 |

**样式规则：**
- 与 AppointmentCard 保持一致的卡片样式
- 无点击跳转（纯展示）
- 批次号区域使用 `$color-bg-page` 背景

## 错误处理与空状态设计

### 全局错误拦截策略

在 `utils/request.js` 响应拦截器中统一处理，页面层仅关注业务逻辑。

| HTTP状态码 | 业务码 | 处理方式 |
|-----------|--------|----------|
| 200 | 200 | 正常返回 data |
| 200 | 非200 | `uni.showToast({ title: message, icon: 'none' })` 显示业务错误 |
| 401 | 1001 | 清除token → 跳转登录页 `uni.redirectTo({ url: '/pages/auth/login' })` |
| 403 | 1002 | `uni.showModal({ title: '无权限', content: '您没有执行此操作的权限' })` |
| 429 | 1006 | `uni.showToast({ title: '操作过于频繁，请稍后再试', icon: 'none' })` |
| 500 | 1007 | `uni.showToast({ title: '系统异常，请稍后重试', icon: 'none' })` |
| 网络断开 | — | 显示网络断开浮层（见下方） |

### 页面级错误处理

#### Loading 态

**首次加载（无缓存数据时）：**

使用 `uni-skeleton` 骨架屏，根据页面内容定制骨架形状。

```
首页骨架屏：                          预约列表骨架屏：
┌─────────────────────┐              ┌─────────────────────┐
│ ░░░░░░░░░░░░░░░░░░ │ ← 标题骨架   │ ░░░░░░░░░░░░░░░░░░ │
│ ░░░░░░░░░░░░░░░░░░ │ ← 轮播骨架   │ ░░░░░░░░░░░░░░░░░░ │
│                     │              ├─────────────────────┤
│ ░░░░░░░░░░░░░░░░░░ │ ← 卡片骨架   │ ░░░░░░░░░░░░░░░░░░ │
│ ░░░░░░░░░░░░░░░░░░ │              ├─────────────────────┤
│ ░░░░░ ░░░░░░ ░░░░░ │ ← 网格骨架   │ ░░░░░░░░░░░░░░░░░░ │
│ ░░░░░ ░░░░░░ ░░░░░ │              └─────────────────────┘
└─────────────────────┘
```

**骨架屏规范：**
- 背景色：`$color-border-light`（#F0F0F0）
- 动画：`uni-skeleton` 默认闪烁动画
- 骨架元素宽高与真实内容一致
- 骨架屏在数据返回后立即替换为真实内容，无过渡动画

**局部刷新（已有数据，更新中）：**

不显示骨架屏，仅在操作按钮上显示 loading 态：
- 按钮文字变为"提交中..."，禁用点击
- 使用按钮组件的 `loading` 属性

#### 空数据态

使用 `EmptyState` 组件，各页面定制提示文案和操作按钮。

| 页面 | 图标 | 标题 | 描述 | 操作按钮 |
|------|------|------|------|----------|
| 首页-今日预约 | `calendar` | 暂无今日预约 | 快去预约疫苗接种吧 | 立即预约 |
| 预约列表 | `list` | 暂无预约记录 | 您还没有预约过疫苗接种 | 立即预约 |
| 儿童列表 | `person` | 暂无儿童信息 | 添加第一位儿童开始使用 | 添加儿童 |
| 疫苗目录-搜索 | `search` | 未找到疫苗 | 试试其他关键词吧 | — |
| 公告列表 | `chat` | 暂无公告 | 目前没有系统公告 | — |

**空状态布局规范：**
- 图标距顶部：页面垂直居中偏上 1/3
- 图标与标题间距：`$spacing-md`
- 标题与描述间距：`$spacing-xs`
- 描述与按钮间距：`$spacing-lg`
- 操作按钮：宽度 240rpx，高度 72rpx

#### 网络错误

**网络断开浮层：**

```
┌─────────────────────────────────┐
│  (页面内容，半透明)              │
│                                 │
│  ┌─────────────────────────┐    │
│  │                         │    │  ← 全屏半透明遮罩
│  │     📡 网络连接已断开     │    │     白色卡片居中
│  │   请检查您的网络设置后    │    │     $shadow-float
│  │      点击重试             │    │
│  │                         │    │
│  │     [ 重新连接 ]         │    │  ← $color-primary 按钮
│  │                         │    │
│  └─────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

**处理规则：**
- 通过 `uni.onNetworkStatusChange` 监听网络状态
- 网络断开时显示浮层，页面内容不变（半透明）
- 点击"重新连接"触发重试，网络恢复后自动关闭浮层并刷新当前页面数据
- 请求超时（10s）同样视为网络错误

**请求失败（非网络原因）：**

单次请求失败时：
- 不影响页面已有内容
- 底部显示轻提示 `uni.showToast`
- 关键操作（如提交预约）失败后保留表单数据，用户可重试

### 表单校验规则

使用 `uni-forms` 组件的内置校验能力，规则定义在页面 `rules` 对象中。

#### 登录表单

| 字段 | 规则 | 错误提示 |
|------|------|----------|
| phone | required + pattern `/^1[3-9]\d{9}$/` | "请输入正确的手机号" |
| password | required + minlength(6) + maxlength(20) | "密码长度为6-20位" |
| smsCode | required + pattern `/^\d{6}$/` | "请输入6位验证码" |

#### 注册表单

| 字段 | 规则 | 错误提示 |
|------|------|----------|
| phone | required + pattern `/^1[3-9]\d{9}$/` | "请输入正确的手机号" |
| smsCode | required + pattern `/^\d{6}$/` | "请输入6位验证码" |
| password | required + minlength(6) + maxlength(20) + pattern `/^(?=.*[a-zA-Z])(?=.*\d)/` | "密码需包含字母和数字，长度6-20位" |
| confirmPassword | required + validate(password === confirmPassword) | "两次输入的密码不一致" |
| realName | required + minlength(2) + maxlength(50) + pattern `/^[\u4e00-\u9fa5·]+$/` | "请输入2-50位中文姓名" |

#### 儿童表单

| 字段 | 规则 | 错误提示 |
|------|------|----------|
| name | required + minlength(2) + maxlength(50) | "请输入儿童姓名" |
| gender | required | "请选择性别" |
| birthDate | required + validate(<= today) | "请选择出生日期（不可超过今天）" |
| idCardType | required | "请选择证件类型" |
| idCardNo | conditional(required) + pattern（身份证18位/护照） | "请输入正确的证件号码" |

**表单校验交互规范：**
- 提交时触发全量校验，校验失败时：
  - 第一个错误字段自动滚动到可视区域
  - 错误字段下方显示红色提示文字（`$color-danger`，`$font-size-xs`）
  - 错误字段输入框边框变红
- 输入时实时校验（`validateTrigger: 'bind'`）：用户修正后立即清除错误提示
- 密码字段显示/隐藏切换（眼睛图标）

### 权限拦截

**路由守卫逻辑（`App.vue` 或路由拦截）：**

```
页面访问 → 检查token → 有效 → 放行
                      → 无效 → 检查是否为公开页面
                                → 是 → 放行
                                → 否 → 跳转登录页，记录来源页
```

**公开页面（无需登录）：**
- `pages/auth/login`
- `pages/auth/register`
- `pages/auth/forgot-password`
- `pages/vaccine/list`
- `pages/vaccine/detail`
- `pages/notice/list`
- `pages/notice/detail`

**受保护页面（需登录）：**
- 所有其他页面

**Token 过期处理：**
- 请求返回 401 → 清除 token → 跳转登录页
- 登录页 `onLoad` 检查 URL 参数 `redirect`，登录成功后跳转回原页面

### 404兜底

无效路由（未在 `pages.json` 中注册的路径）由 uni-app 框架自动跳转到 `pages/common/404`，显示"页面不存在"提示和返回首页按钮。

### 冻结状态处理

用户被冻结（爽约3次/账号禁用）时的处理：

| 冻结类型 | 触发条件 | 前端表现 |
|----------|----------|----------|
| 预约冻结 | `no_show_count >= 3` | 预约相关操作按钮禁用，显示"您已被冻结预约资格，剩余X天"提示条 |
| 账号禁用 | `user.status = 1` | 强制退出登录，下次登录时弹窗提示"账号已被禁用，请联系管理员" |

**冻结提示条样式：**
```
┌─────────────────────────────────┐
│ ⚠ 您的预约资格已被冻结            │  ← $color-warning 背景, 白色文字
│   解冻时间：2026-04-11           │     高度 auto, 内边距 $spacing-base
└─────────────────────────────────┘
```
- 位置：页面顶部（导航栏下方），固定定位
- 仅在预约相关页面显示

### 业务错误码映射（用户端）

以下错误码需特殊处理（非默认 toast 展示）：

| 错误码 | 场景 | 处理方式 |
|--------|------|----------|
| 2005 | 时段已满 | toast "该时段名额已满，请选择其他时段" |
| 2006 | 重复预约 | toast "该儿童已预约此疫苗，请勿重复预约" |
| 2007 | 无法取消 | toast "当前状态不允许取消预约" |
| 2009 | 预约已过期 | toast "该预约已过期" |
| 7001 | 手机号已注册 | toast "该手机号已注册，请直接登录" |
| 7002 | 登录失败 | toast "手机号或密码错误"（不区分是手机号错还是密码错） |
| 7007 | 验证码错误 | toast "验证码错误或已过期" |
| 7008 | 账号锁定 | toast "密码错误次数过多，账号已锁定30分钟" |
| 7011 | 儿童数量超限 | toast "最多管理5位儿童" |
| 7012 | 儿童有进行中预约 | toast "该儿童有进行中的预约，无法删除" |
| 1011 | 账号冻结 | toast "账号已被冻结，请联系管理员" |
| 1012 | 预约冻结 | 显示冻结提示条 |

### 乐观更新策略

**适用场景：** 取消预约（操作简单，状态明确）

```
用户点击取消 → 确认弹窗 → 确认
→ 立即更新本地状态为"已取消"（UI先响应）
→ 发送取消请求
→ 成功：无额外操作
→ 失败：回滚本地状态 + 显示错误toast + 允许重试
```

**不适用场景：** 创建预约（需要后端返回预约编号等完整数据）

### 确认弹窗规范

需要二次确认的操作统一使用 `uni.showModal`：

| 操作 | 标题 | 内容 | 确认按钮 |
|------|------|------|----------|
| 取消预约 | 取消预约 | 确定要取消该预约吗？取消后需重新预约 | 取消 |
| 删除儿童 | 删除儿童 | 确定要删除该儿童信息吗？删除后不可恢复 | 删除（红色） |
| 退出登录 | 退出登录 | 确定要退出当前账号吗？ | 退出 |
| 返回（表单有修改） | 放弃编辑 | 当前内容未保存，确定要返回吗？ | 放弃 |

**确认按钮样式规范：**
- 危险操作（删除）：确认按钮使用 `$color-danger`
- 普通操作（取消预约、退出）：确认按钮使用 `$color-primary`
- 取消按钮统一使用默认样式

## 页面导航与传参规范

### 路由跳转方式

uni-app 提供 5 种页面跳转 API，本项目中按场景选择：

| API | 说明 | 使用场景 |
|-----|------|----------|
| `uni.navigateTo` | 保留当前页，跳入新页（入栈） | TabBar 以外的页面间跳转 |
| `uni.redirectTo` | 关闭当前页，跳入新页（替换） | 登录成功后、修改密码成功后（不希望返回） |
| `uni.switchTab` | 跳转到 TabBar 页（清空栈） | 跳转首页/预约/我的 |
| `uni.navigateBack` | 返回上一页（出栈） | 返回按钮 |
| `uni.reLaunch` | 关闭所有页面，打开新页 | 退出登录后跳登录页 |

### 页面跳转路径汇总

| 来源 | 目标 | 跳转方式 | 传参 |
|------|------|----------|------|
| 登录页 | 首页 | `redirectTo` | — |
| 注册页 | 登录页 | `navigateBack` | — |
| 忘记密码 | 登录页 | `redirectTo` | — |
| 修改密码 | 登录页 | `reLaunch` | — |
| 首页-快捷入口 | 创建预约 | `navigateTo` | — |
| 首页-快捷入口 | 儿童列表 | `navigateTo` | — |
| 首页-快捷入口 | 疫苗目录 | `navigateTo` | — |
| 首页-快捷入口 | 接种记录 | `navigateTo` | — |
| 首页-快捷入口 | 公告列表 | `navigateTo` | — |
| 首页-今日预约卡片 | 预约详情 | `navigateTo` | `id=预约ID` |
| 预约列表 | 创建预约 | `navigateTo` | — |
| 预约列表 | 预约详情 | `navigateTo` | `id=预约ID` |
| 预约详情 | 流程指引 | `navigateTo` | `id=预约ID` |
| 创建预约-选儿童 | 添加儿童 | `navigateTo` | `from=create` |
| 个人中心 | 儿童列表 | `navigateTo` | — |
| 个人中心 | 修改密码 | `navigateTo` | — |
| 个人中心 | 接种记录 | `navigateTo` | — |
| 儿童列表 | 添加/编辑儿童 | `navigateTo` | `id=儿童ID`（编辑时传） |
| 疫苗目录 | 疫苗详情 | `navigateTo` | `id=疫苗ID` |
| 疫苗详情 | 创建预约 | `navigateTo` | `vaccineId=疫苗ID` |
| 公告列表 | 公告详情 | `navigateTo` | `id=公告ID` |
| 公告详情 | 公告反馈 | `navigateTo` | `noticeId=公告ID&title=公告标题` |
| 任意受保护页 | 登录页 | `redirectTo` | `redirect=当前页路径` |

### 参数传递规范

**简单参数（ID、来源标记等）：** 使用 URL query string

```javascript
// 传递
uni.navigateTo({ url: `/pages/appointment/detail?id=${id}` })

// 接收
onLoad(options) {
  const id = options.id
}
```

**复杂参数（对象、列表等）：** 使用全局状态（Pinia Store）或 `uni.$emit` 事件

```javascript
// 传递（如创建预约时选中的儿童和疫苗信息）
useCreateAppointmentStore().selectedChild = child

// 接收
const store = useCreateAppointmentStore()
const child = store.selectedChild
```

**参数校验：**
- `onLoad` 中校验必要参数是否存在，缺失则 `uni.showToast` 提示后返回

```javascript
onLoad(options) {
  if (!options.id) {
    uni.showToast({ title: '参数错误', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 1500)
    return
  }
}
```

### 页面栈管理

| 场景 | 页面栈状态 | 说明 |
|------|-----------|------|
| 登录 → 首页 | [首页] | `redirectTo` 替换登录页 |
| 首页 → 预约列表 → 预约详情 → 流程指引 | [首页, 列表, 详情, 指引] | 最多 4 层 |
| 首页 → 创建预约 → 添加儿童 | [首页, 创建, 添加] | 3 层 |
| TabBar 切换 | [目标Tab页] | `switchTab` 清空栈 |

**栈深度限制：** uni-app 页面栈最多 10 层。本项目最深路径为 4 层（首页→列表→详情→指引），安全范围内。

### 登录拦截与重定向

**拦截时机：** `App.vue` 的 `onShow` 生命周期中统一检查。

**拦截逻辑：**

```javascript
// App.vue
onShow() {
  const token = getToken()
  const currentPath = getCurrentPath()

  if (!token && !isPublicPage(currentPath)) {
    // 记录来源页，登录后跳回
    uni.redirectTo({
      url: `/pages/auth/login?redirect=${encodeURIComponent(currentPath)}`
    })
  }
}
```

**公开页面列表：** `pages/auth/login`、`pages/auth/register`、`pages/auth/forgot-password`、`pages/vaccine/list`、`pages/vaccine/detail`、`pages/notice/list`、`pages/notice/detail`

**登录后跳回：**

```javascript
// pages/auth/login
onLoad(options) {
  this.redirectUrl = options.redirect ? decodeURIComponent(options.redirect) : '/pages/index/index'
}
// 登录成功后
loginSuccess() {
  uni.switchTab({ url: this.redirectUrl.startsWith('/pages/') && isTabPage(this.redirectUrl)
    ? this.redirectUrl
    : '/pages/index/index' })
}
```

> 注意：`switchTab` 只能跳转 TabBar 页，非 TabBar 页用 `redirectTo`。

## 项目改造步骤

1. 清除demo页面和组件，保留`uni_modules`
2. 替换Vuex为Pinia
3. 重写`pages.json`（新页面路由 + TabBar配置）
4. 重写`App.vue`（token检查、登录状态维护）
5. 重写`main.js`
6. 创建`api/`、`utils/`、`hooks/`目录和基础设施代码
7. 按模块实现页面

## 约束

- 用户最多管理5个儿童档案
- 预约最多提前7天
- 预约取消仅限"待签到"状态
- 删除儿童仅限无进行中预约
- 留观最少30分钟
- 短信验证码60秒间隔限制
- 爽约3次冻结7天（仅限预约，不影响登录）
