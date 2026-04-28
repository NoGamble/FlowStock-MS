<p align="center">
  <img src="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>📦</text></svg>" width="80" alt="logo">
</p>

<h1 align="center">FlowStock-MS</h1>
<p align="center">A clean, modern Inventory Management System built with <strong>Spring Boot 3</strong> and <strong>Vue 3</strong>.</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=spring-boot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.x-4FC08D?logo=vue.js&logoColor=white" alt="Vue 3">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/license-MIT-blue" alt="License">
</p>

---

## ✨ Features

<table>
  <tr>
    <td width="50%">
      <h4>📊 Dashboard</h4>
      <p>Real-time metrics strip, inbound/outbound trend chart, category pie chart, and low-stock alerts — all with animated counters.</p>
    </td>
    <td width="50%">
      <h4>📦 Product Management</h4>
      <p>Full CRUD with SKU codes, real-time search filtering, and color-coded stock level tags.</p>
    </td>
  </tr>
  <tr>
    <td>
      <h4>📥 Inbound Console</h4>
      <p>Search products, batch quantity input with quick-add buttons, and live inventory preview before confirming.</p>
    </td>
    <td>
      <h4>📤 Outbound Console</h4>
      <p>Symmetric design with over-stock validation and real-time quantity checks.</p>
    </td>
  </tr>
  <tr>
    <td>
      <h4>🔍 Stocktake</h4>
      <p>Digital audit workflow — record actual counts, auto-calculate profit/loss, browse historical records.</p>
    </td>
    <td>
      <h4>🔒 Concurrency Safe</h4>
      <p>Optimistic locking via JPA <code>@Version</code> prevents race conditions on inventory operations.</p>
    </td>
  </tr>
</table>

## 🚀 Quick Start

**Prerequisites:** JDK 17+ · Node.js 18+ · MySQL 8.0

```bash
# 1. Create the database
mysql -u root -e "CREATE DATABASE IF NOT EXISTS flowstock_db"

# 2. Edit database credentials in src/main/resources/application.yaml

# 3. Start both backend & frontend
./dev.sh
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |

Vite proxies `/api` requests to the backend automatically — no CORS issues in development.

## 🧱 Project Structure

```
FlowStock-MS/
├── src/main/java/com/flowstock/ms/
│   ├── controller/          REST controllers
│   ├── service/             Business logic (@Transactional)
│   ├── repository/          Spring Data JPA interfaces
│   ├── entity/              JPA entities
│   ├── dto/                 Result<T>, MovementRequest, StocktakeResponse
│   └── config/              CORS, GlobalExceptionHandler
├── frontend/
│   └── src/
│       ├── api/             Axios modules (product, movement, stocktake)
│       ├── views/           Page components (5 pages)
│       ├── layouts/         MainLayout with collapsible sidebar
│       └── router/          Vue Router config
├── dev.sh                   One-click dev launcher
└── pom.xml                  Maven config
```

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.5, Java 17, Maven |
| Database | MySQL 8.0, Spring Data JPA (Hibernate) |
| Frontend | Vue 3 (Composition API), Vite 7 |
| UI | Naive UI (auto-imported components) |
| Charts | ECharts 6 via vue-echarts |
| HTTP | Axios with response interceptor |

## 📡 API Overview

All endpoints return a uniform `Result<T>` envelope:

```json
{ "code": 200, "message": "操作成功", "data": {} }
```

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products` | List all products |
| `POST` | `/api/products` | Create a product |
| `PUT` | `/api/products/{id}` | Update product info |
| `DELETE` | `/api/products/{id}` | Delete a product |
| `POST` | `/api/stock-movements/inbound` | Record inbound |
| `POST` | `/api/stock-movements/outbound` | Record outbound |
| `GET` | `/api/stocktakes` | List stocktake records |
| `POST` | `/api/stocktakes` | Execute stocktake |
| `DELETE` | `/api/stocktakes/{id}` | Delete a record |

## 🏗 Build & Deploy

```bash
# Production build
cd frontend && npm install && npm run build
cp -r frontend/dist/* src/main/resources/static/
./mvnw clean package -DskipTests

# Run
java -jar target/FlowStock-MS-0.0.1-SNAPSHOT.jar
```

## 📄 License

MIT — built by [NoGamble](https://github.com/NoGamble).
