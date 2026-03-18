# FlowStock-MS 📦

An efficient Inventory Management System (IMS) built with a modern full-stack architecture. This project features a robust **Spring Boot 3** backend and a high-performance **Vue 3** frontend.

[![Tech Stack](https://img.shields.io/badge/Stack-Spring_Boot_3_+_Vue_3-blue)](https://github.com/your-username/FlowStock-MS)

---

## 🏗 Project Structure

The project follows a **Monorepo** directory structure for streamlined development and deployment:

```text
FlowStock-MS/
├── src/main/java/        # Spring Boot Backend (Java 17)
├── src/main/resources/   # Backend configurations (YAML, SQL)
├── frontend/             # Vue 3 Frontend (Vite, Naive UI)
├── dev.sh                # Local development startup script
├── pom.xml               # Maven configuration
└── README.md             # Project documentation
````

-----

## 🛠 Tech Stack

### Backend

* **Framework:** Spring Boot 3.x
* **Language:** Java 17
* **Database:** MySQL 8.x
* **ORM:** Spring Data JPA
* **Build Tool:** Maven

### Frontend

* **Framework:** Vue 3 (Composition API)
* **UI Library:** Naive UI
* **Build Tool:** Vite
* **State Management:** Pinia (optional)
* **HTTP Client:** Axios

-----

## 🚀 Getting Started

### Prerequisites

* **JDK 17** or higher
* **Node.js 18** or higher
* **MySQL 8.0**
* **Maven 3.8+**

### 1\. Database Setup

1.  Create a database named `flowstock_db` in your MySQL instance.
2.  Update the credentials in `src/main/resources/application.yaml`:
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/flowstock_db
        username: your_username
        password: your_password
    ```

### 2\. Quick Launch (Development Mode)

We provide a one-click script to start both the backend and frontend simultaneously:

```bash
# Grant execution permission
chmod +x dev.sh

# Run the development environment
./dev.sh
```

* **Backend:** [http://localhost:8080](https://www.google.com/search?q=http://localhost:8080)
* **Frontend:** [http://localhost:5173](https://www.google.com/search?q=http://localhost:5173)

-----

## 📦 Production Deployment

To package the entire application into a single executable JAR file:

1.  **Build Frontend:**
    ```bash
    cd frontend && npm install && npm run build
    ```
2.  **Sync Static Files:** Copy `frontend/dist/*` to `src/main/resources/static/`.
3.  **Package Backend:**
    ```bash
    ./mvnw clean package -DskipTests
    ```
4.  **Run:**
    ```bash
    java -jar target/FlowStock-MS-0.0.1-SNAPSHOT.jar
    ```

-----

## 📝 Features

- [x] **Inventory Dashboard**: Real-time stock status visualization.
- [x] **Product Management**: CRUD operations for warehouse items.
- [x] **Stock Movement**: Track inbound and outbound logistics.
- [x] **Stocktake System**: Digital record-keeping for inventory audits.

-----

## 🤝 Contributing

Developed by **NoGamble** (@UESTC). Feel free to submit a Pull Request or open an Issue.

-----

