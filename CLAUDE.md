# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Start both backend (8080) and frontend (5173) simultaneously
./dev.sh

# Backend only
./mvnw spring-boot:run                         # dev mode with hot reload (spring-boot-devtools)
./mvnw clean package -DskipTests               # production JAR in target/

# Frontend only
cd frontend && npm run dev                     # Vite dev server with HMR

# Tests
./mvnw test                                    # all tests
./mvnw test -Dtest=ConcurrencyTest             # single test class
```

The database is MySQL 8.x (`flowstock_db`). Credentials in `src/main/resources/application.yaml`. JPA uses `ddl-auto: update`, so tables are auto-created from entity definitions.

## Architecture

**Backend** — Spring Boot 3.5.11, Java 17, Spring Data JPA, Maven. Standard layered architecture:

```
controller/          REST controllers (@RestController). Return Result<T> directly.
                     Endpoints: /api/products, /api/stock-movements, /api/stocktakes
service/             Business logic, @Transactional boundaries
repository/          Spring Data JPA interfaces extending JpaRepository
entity/              JPA entities mapped to MySQL tables
dto/                 Result<T> (uniform response), MovementRequest, StocktakeResponse
config/              WebConfig (CORS), GlobalExceptionHandler (@RestControllerAdvice)
```

**Frontend** — Vue 3 Composition API, Naive UI, Vite 7, vue-router. Naive UI components are auto-imported (no manual imports needed). Vite proxies `/api` to `localhost:8080`.

```
src/api/             Axios API functions per domain + request.js (base instance)
src/views/           Page components: dashboard, product, inbound, outbound, stocktake
src/layouts/         MainLayout.vue — sidebar menu + router-view
src/router/          Route definitions
```

## API response convention

All backend endpoints return `Result<T>`: `{ code: 200, message: "操作成功", data: T }`. Errors return non-200 codes with messages. The frontend Axios interceptor in `request.js` extracts `.data` on success (code 200) and alerts on failure — so frontend API callers receive the unwrapped payload directly.

## Concurrency

Inventory entity uses JPA `@Version` for optimistic locking. The `GlobalExceptionHandler` catches `ObjectOptimisticLockingFailureException` (409), `CannotAcquireLockException` (408), and `RuntimeException` (500) — all mapped to `Result<Void>`. Stock movement operations are `@Transactional`.
```
