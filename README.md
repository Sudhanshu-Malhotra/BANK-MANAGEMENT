# Digital Banking Backend (Spring Boot Microservices)

Welcome to the Digital Banking Backend project! This project demonstrates building a scalable microservices architecture from scratch using Java 21, Spring Boot, Spring Cloud, and MySQL.

The project is structured step-by-step up to Phase 4.

## Phases Implemented

*   **Phase 1: Basic Microservices (User & Account)**
    *   Created `user-service` (Port 8081) and `account-service` (Port 8082).
    *   Setup layered architecture (Controllers, Services, Repositories).
    *   Integrated with MySQL databases (`user_db` and `account_db`).
*   **Phase 2: JWT Authentication**
    *   Added Spring Security to `user-service`.
    *   Passwords are encrypted using `BCrypt`.
    *   Endpoints are protected, requiring a Bearer Token (JWT).
    *   Added `/api/auth/register` and `/api/auth/login`.
*   **Phase 3: API Gateway**
    *   Created `api-gateway` (Port 8080).
    *   Acts as a single entry point for all frontend/mobile apps.
    *   Automatically routes `/api/users/**` and `/api/auth/**` to `user-service`.
    *   Automatically routes `/api/accounts/**` to `account-service`.
*   **Phase 4: Service Discovery**
    *   Created `eureka-server` (Port 8761).
    *   Services dynamically register themselves under names (`USER-SERVICE`, `ACCOUNT-SERVICE`).
    *   The API Gateway uses these names to route traffic without hardcoding ports.

---

## Architecture Overview

1.  **Eureka Server (8761)**: First service to start. It acts as a phonebook.
2.  **API Gateway (8080)**: Second service to start. Clients only talk to this service.
3.  **User Service (8081)**: Manages users and Authentication (JWT). Registers with Eureka.
4.  **Account Service (8082)**: Manages financial accounts, deposits, and withdrawals. Registers with Eureka.

---

## How to Run the Project

You need **Java 21**, **Gradle**, and **MySQL** running locally (User: `root`, Password: `Malhotra@13`).

Open exactly 4 separate terminals and start the services in this order:

### 1. Start Eureka Server
```bash
cd eureka-server
./gradlew bootRun
```
> Wait until you see "Tomcat started on port 8761"

### 2. Start API Gateway
```bash
cd api-gateway
./gradlew bootRun
```

### 3. Start User Service
```bash
cd user-service
./gradlew bootRun
```

### 4. Start Account Service
```bash
cd account-service
./gradlew bootRun
```

Wait a few moments for the microservices to register with Eureka. You can verify they are running by checking `http://localhost:8761/` in your browser.

---

## API Testing (Postman / CURL Commands)

*Important: Since we added the API Gateway on port 8080, we will ONLY send requests to `localhost:8080`! The Gateway will do the heavy lifting of routing it.*

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
-H "Content-Type: application/json" \
-d '{"name":"Super", "email":"super@example.com", "password":"password123"}'
```

### 2. Login to get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"super@example.com", "password":"password123"}'
```
*(Copy the long text output. This is your JWT Token. We will refer to it as `<YOUR_TOKEN>` below).*

### 3. Get All Users (Secured)
Replace `<YOUR_TOKEN>` with the token received from the login step.
```bash
curl -X GET http://localhost:8080/api/users \
-H "Authorization: Bearer <YOUR_TOKEN>"
```

### 4. Create a Financial Account for User ID 1
```bash
curl -X POST http://localhost:8080/api/accounts/user/1
```

### 5. Deposit Money (For Account ID 1)
```bash
curl -X POST "http://localhost:8080/api/accounts/1/deposit?amount=500"
```

### 6. Withdraw Money (For Account ID 1)
```bash
curl -X POST "http://localhost:8080/api/accounts/1/withdraw?amount=200"
```

### 7. View Account Details
```bash
curl -X GET http://localhost:8080/api/accounts/1
```

Congratulations! You have successfully stepped through and built a complete Digital Banking Backend with Microservices, Security, Routing, and Discovery.
