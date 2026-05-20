# 疫苗管理系统 V2

面向大型医院的综合性疫苗预防接种管理平台，支持在线预约、现场流程管理、窗口叫号及库存监控全业务闭环。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 / Spring Boot 3.2.5 / MyBatis-Plus 3.5.7 / MySQL 8.0 / Redis / Spring Security + JWT |
| 前端 | UniApp / Vue 3 / Pinia / Vite / Sass |
| API 文档 | Knife4j (OpenAPI 3) |
| 架构模式 | DDD（领域驱动设计） |

## 项目结构

```
vaccine_systemV2_project/
├── docs/                           # 项目文档
│   ├── 01-SRD/                     # 业务规格说明书
│   ├── 02-PRD/                     # 产品需求文档
│   ├── 03-REQ/                     # 详细需求
│   ├── 04-DOMAIN/                  # 领域模型设计
│   └── 05-DESIGN/                  # 技术设计（API / 数据库 / 测试数据）
├── vaccine_systemV2/               # 后端（Spring Boot 多模块）
│   ├── vaccine-common/             # 共享内核：统一响应、异常、枚举、常量
│   ├── vaccine-domain/             # 领域层：实体、值对象、领域服务、仓储接口
│   ├── vaccine-application/        # 应用层：应用服务、DTO、装配器
│   ├── vaccine-infrastructure/     # 基础设施层：MyBatis-Plus、Redis、JWT 实现
│   ├── vaccine-adapter/            # 适配层：Controller、全局异常处理
│   └── vaccine-start/              # 启动模块：Spring Boot 入口、配置文件
└── vaccine_systemV2app/            # 前端（UniApp 跨平台）
    ├── pages/                      # 页面（67 个）
    ├── api/                        # 接口请求封装
    ├── store/                      # Pinia 状态管理
    ├── components/                 # 公共组件
    └── hooks/                      # 组合式函数
```

## 核心业务

### 六阶段接种流程

```
签到 → 预检 → 登记 → 接种 → 留观 → 完成
```

预约是核心聚合根，所有业务流程围绕预约记录驱动，通过状态机严格控制流转。

### 三端用户

| 端 | 角色 | 功能 |
|----|------|------|
| 家长端 | 家长 | 在线预约、排队叫号、接种记录查看、留观倒计时 |
| 医生端 | 预检医生 / 登记医生 / 接种医生 / 留观医生 | 流程操作、叫号管理、不良反应上报 |
| 管理员端 | 业务管理员 / 系统管理员 | 排班管理、窗口配置、疫苗目录维护、角色权限、统计报表 |

### 关键特性

- **窗口制运营** — 多窗口动态配置，按功能分配医生
- **FEFO 库存管理** — 先到期先出，登记时锁定、接种时扣减的两阶段库存控制
- **精细权限** — 10 种角色、48 项权限，严格的 RBAC 隔离
- **实时引导** — 系统根据预约状态自动引导家长前往下一窗口

## 领域模型

系统划分为 8 个限界上下文：

| 上下文 | 聚合根 | 职责 |
|--------|--------|------|
| 预约上下文 | Appointment | 预约创建、取消、过期、状态机 |
| 流程指引上下文 | 无（纯查询） | 窗口引导、排队计算 |
| 登记上下文 | RegisterQueue | 登记核验、FEFO 批次锁定、队列管理 |
| 接种上下文 | VaccinationRecord | 接种执行、库存扣减、注射号生成 |
| 留观上下文 | ObserveRecord | 留观监控、预检评估、不良反应处理 |
| 库存上下文 | VaccineBatch / HospitalVaccineSummary | 批次管理、库存调拨、报废、预警 |
| 身份认证上下文 | User / ChildProfile / Role | 用户认证、RBAC 权限、儿童档案 |
| 共享内核 | — | 状态枚举、错误码、权限码 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0
- Redis 6.0+
- Node.js 18+
- HBuilderX（前端开发）

### 后端启动

```bash
# 1. 创建数据库并导入表结构
mysql -u root -p < docs/05-DESIGN/test_data.sql

# 2. 修改数据库和 Redis 连接配置
#    编辑 vaccine_systemV2/vaccine-start/src/main/resources/application.yml

# 3. 编译并启动
cd vaccine_systemV2
./mvnw spring-boot:run -pl vaccine-start
```

启动后访问 API 文档：`http://localhost:8080/doc.html`

### 前端启动

```bash
cd vaccine_systemV2app
npm install
npm run dev:h5        # H5 开发模式
```

使用 HBuilderX 打开 `vaccine_systemV2app/` 目录可编译为 App 或小程序。

## 数据库

MySQL 8.0，共 27 张表，数据库名 `vaccine_db`。

设计原则：无物理外键（应用层校验）、统一 BIGINT 主键、乐观锁并发控制、逻辑删除预留。

## API

- 基础路径：`/api/v1`
- 认证方式：JWT Bearer Token
- 响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1700000000000
}
```

## 文档

详细设计文档位于 `docs/` 目录，覆盖从业务规格到技术实现的完整设计链路。
