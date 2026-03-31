# 🏦 Enterprise Banking Backend

> **Production-grade** microservices banking system built with **Java 21**, **Spring Boot 4.0**, **Spring Cloud 2025.1**, Kafka, Redis, Resilience4j, and Prometheus/Grafana.

---

## 🏛️ Architecture Overview

```
Internet
    │
    ▼ :8080
┌─────────────────────────────────────────────────┐
│              API GATEWAY                        │
│   JWT Auth Filter  ·  Spring Cloud Gateway      │
│   CORS  ·  Load Balancing  ·  Rate Limiting     │
└────────────┬────────────────────────────────────┘
             │  Eureka Service Discovery (lb://)
  ┌──────────┼──────────────────────────┐
  │          │              │           │
  ▼ :8081    ▼ :8082        ▼ :8083     ▼ :8084
USER-SVC  ACCOUNT-SVC  TRANSACTION   NOTIFICATION
JWT+BCrypt  Redis Cache  Feign+R4j CB  Kafka Consumer
MySQL       MySQL        MySQL         (event-driven)
  │            ▲              │
  └────────────┘              │
   Kafka: userRegistration    │
                              ▼
                     Kafka: transactionTopic ──► NOTIFICATION
```

### Services

| Service | Port | Responsibilities |
|---|---|---|
| **api-gateway** | 8080 | JWT auth, routing, CORS, load balancing |
| **eureka-server** | 8761 | Service discovery & registry |
| **user-service** | 8081 | Registration, login, JWT generation |
| **account-service** | 8082 | Accounts, deposits, withdrawals, Redis cache |
| **transaction-service** | 8083 | Transfers via Feign + Resilience4j circuit breaker |
| **notification-service** | 8084 | Async Kafka consumer for alerts |

### Infrastructure

| Component | Port | Purpose |
|---|---|---|
| MySQL | 3307 | Separate DB per service |
| Kafka | 9092 | Event streaming (user registration, transactions) |
| Redis | 6379 | Account & user data caching |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3000 | Dashboards & monitoring |

---

## 🚀 Quick Start (Docker — Recommended)

### Prerequisites
- Docker Desktop running
- 8 GB RAM available

### 1. Configure Secrets
```bash
cd docker
cp .env.example .env
# The defaults in .env.example work for local development
```

### 2. Start Everything
```bash
docker-compose up --build -d
```

This boots **11 containers** in the correct dependency order (health-check gated). Gradle builds happen inside Docker via multi-stage builds — no local Java needed.

### 3. Verify Everything Is Up
```
Eureka Dashboard  →  http://localhost:8761          (all 5 services registered)
Prometheus        →  http://localhost:9090
Grafana           →  http://localhost:3000          (admin / Malhotra@13)
API Gateway       →  http://localhost:8080/actuator/health
```

> **First boot takes 3–5 minutes.** Gradle downloads dependencies inside Docker, and MySQL needs ~30s to initialize.

---

## 🔌 API Reference

Base URL: `http://localhost:8080`  
All secured endpoints require `Authorization: Bearer <TOKEN>`.

### Authentication

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:** `201 Created`
```json
"User registered successfully"
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "john@example.com"
}
```

---

### Accounts

```http
# Create account for user
POST /api/accounts/user/{userId}
Authorization: Bearer <TOKEN>

# Get account by ID
GET /api/accounts/{id}
Authorization: Bearer <TOKEN>

# Get all accounts for user
GET /api/accounts/user/{userId}
Authorization: Bearer <TOKEN>

# Deposit money
POST /api/accounts/{id}/deposit?amount=500.00
Authorization: Bearer <TOKEN>

# Withdraw money
POST /api/accounts/{id}/withdraw?amount=200.00
Authorization: Bearer <TOKEN>
```

---

### Transactions

```http
# Transfer between accounts
POST /api/transactions/transfer
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 150.00
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 150.00,
  "type": "TRANSFER",
  "status": "COMPLETED",
  "timestamp": "2026-03-31T14:00:00"
}
```

---

## 🔁 Event Flow

### User Registration → Auto Account Creation
```
POST /api/auth/register
     │
     ▼ user-service
  Saves User to DB
     │
     ▼ Kafka: "userRegistrationTopic"  (userId)
     │
     ▼ account-service
  Auto-creates Account (balance = $0.00)
```

### Transfer → Notification
```
POST /api/transactions/transfer
     │
     ▼ transaction-service
  Saves TX (status=PENDING)
     │
     ├─ Feign → account-service: withdraw(sourceId, amount)
     ├─ Feign → account-service: deposit(destinationId, amount)
     │
  Updates TX (status=COMPLETED)
     │
     ▼ Kafka: "transactionTopic"
     │
     ▼ notification-service
  Logs transfer confirmation (extend: email/SMS)
```

---

## 🔐 Security

- **JWT Bearer tokens** — validated at API Gateway (stateless)
- **BCrypt** password encoding (strength 10)
- **Secret externalized** via `JWT_SECRET` environment variable
- **Input validation** on all request bodies (Bean Validation)
- **Non-root Docker containers** (CIS Docker Benchmark compliant)

---

## 🛡️ Resilience

The `transaction-service` wraps all `account-service` calls with:

| Feature | Config |
|---|---|
| Circuit Breaker | Opens at 50% failure rate, 10s window |
| Auto Half-Open | After 15s, 5 probe requests |
| Retry | 3 attempts, 1s backoff |
| Timeout | 10s per Feign call |

---

## 📊 Monitoring

Every service exposes:
- `GET /actuator/health` — readiness/liveness
- `GET /actuator/metrics` — JVM, HTTP, Kafka metrics
- `GET /actuator/prometheus` — Prometheus scrape endpoint

### Prometheus Scrape Config
Add to `docker/prometheus.yml`:
```yaml
scrape_configs:
  - job_name: 'user-service'
    static_configs:
      - targets: ['user-service:8081']
    metrics_path: /actuator/prometheus
```

---

## 🧱 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Virtual Threads ready) |
| Framework | Spring Boot 4.0.5 |
| Cloud | Spring Cloud 2025.1.1 |
| Security | Spring Security + JJWT 0.12.6 |
| Database | MySQL 8.0 + Spring Data JPA |
| Cache | Redis 7 + Spring Cache |
| Messaging | Apache Kafka (Confluent 7.4.4) |
| Service Mesh | Eureka + Spring Cloud Gateway + OpenFeign |
| Resilience | Resilience4j (CB + Retry + TimeLimiter) |
| Mapping | MapStruct 1.5.5 |
| Monitoring | Micrometer + Prometheus + Grafana |
| Containers | Docker (eclipse-temurin:21-jre-alpine) |
| Build | Gradle 9.4.1 |

---

## 🗂️ Project Structure

```
BANK PROJECT/
├── docker/
│   ├── docker-compose.yml       # Full stack orchestration
│   ├── .env.example             # Secret template
│   └── prometheus.yml           # Prometheus scrape config
├── infrastructure/
│   ├── eureka-server/           # Service discovery
│   └── api-gateway/             # JWT auth + routing
├── services/
│   ├── user-service/            # Auth + users
│   ├── account-service/         # Banking accounts
│   ├── transaction-service/     # Transfers + circuit breaker
│   └── notification-service/    # Kafka-driven alerts
├── settings.gradle
└── build.gradle
```

---

## 📝 Development Notes

- Run with `SPRING_PROFILES_ACTIVE=dev` locally — uses `localhost` defaults
- Run with `SPRING_PROFILES_ACTIVE=prod` in Docker — uses env variable overrides
- Each service has its own MySQL database (auto-created on first boot)
- `ddl-auto: update` in dev, `validate` in prod — never auto-creates schema in production
- JWT expiry: 7 days (configurable via `JWT_EXPIRATION_MS`)
