# FlowStock-MS

智能库存管理系统 — Spring Boot 3 + Vue 3 全栈项目。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.5, Java 17 |
| 数据库 | MySQL 8.x + Spring Data JPA |
| 前端框架 | Vue 3 (Composition API) |
| UI 组件库 | Naive UI |
| 图表 | ECharts 6 (vue-echarts) |
| 构建工具 | Maven (后端) / Vite 7 (前端) |

## 项目结构

```
FlowStock-MS/
├── src/main/java/com/flowstock/ms/
│   ├── controller/       REST 接口 (Product, StockMovement, Stocktake)
│   ├── service/          业务逻辑层
│   ├── repository/       Spring Data JPA 数据访问
│   ├── entity/           JPA 实体 (Inventory, InboundRecord, OutboundRecord, StocktakeRecord)
│   ├── dto/              Result<T> 统一响应, MovementRequest, StocktakeResponse
│   └── config/           CORS 跨域, GlobalExceptionHandler 全局异常处理
├── frontend/
│   ├── src/
│   │   ├── api/          Axios 请求封装 (product, movement, stocktake)
│   │   ├── views/        页面: dashboard, product, inbound, outbound, stocktake
│   │   ├── layouts/      MainLayout 侧边栏布局
│   │   └── router/       路由配置
│   └── index.html
├── dev.sh                一键启动前后端
└── pom.xml
```

## 功能

- **仪表盘** — 库存总览、出入库趋势图、分类占比饼图、低库存预警
- **商品管理** — 商品 CRUD、SKU 编码、搜索过滤、库存颜色标签
- **入库操作台** — 商品搜索、数量输入 + 快捷按钮、库存变化实时预览
- **出库操作台** — 与入库对称设计、超库存校验
- **库存盘点** — 执行盘点、盈亏自动计算、盘点历史记录

## 快速开始

**前置要求：** JDK 17+ / Node.js 18+ / MySQL 8.0

```bash
# 1. 创建数据库
mysql -u root -e "CREATE DATABASE IF NOT EXISTS flowstock_db"

# 2. 修改数据库连接信息
# 编辑 src/main/resources/application.yaml 中的 username / password

# 3. 一键启动
./dev.sh
```

前端 http://localhost:5173 ，后端 http://localhost:8080。Vite 自动代理 `/api` 到后端。

## 单独启动

```bash
# 仅后端 (支持热重载)
./mvnw spring-boot:run

# 仅前端
cd frontend && npm run dev
```

## 打包部署

```bash
cd frontend && npm install && npm run build
cp -r frontend/dist/* src/main/resources/static/
./mvnw clean package -DskipTests
java -jar target/FlowStock-MS-0.0.1-SNAPSHOT.jar
```

## API 规范

所有接口统一返回 `Result<T>`：

```json
{ "code": 200, "message": "操作成功", "data": {} }
```

主要接口：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/api/products` | 商品 CRUD |
| POST | `/api/stock-movements/inbound` | 入库 |
| POST | `/api/stock-movements/outbound` | 出库 |
| GET | `/api/stocktakes` | 盘点记录列表 |
| POST | `/api/stocktakes` | 执行盘点 |

## 并发控制

库存操作使用 JPA `@Version` 乐观锁，`GlobalExceptionHandler` 统一捕获 `ObjectOptimisticLockingFailureException` (409) 和 `CannotAcquireLockException` (408)。
