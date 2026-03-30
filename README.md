# Digital Banking Backend (Spring Boot Monorepo Architect)

Welcome to the Digital Banking Backend! This is a **production-ready**, highly-scalable internal banking ecosystem powered by Java 21, Spring Boot microservices, Spring Cloud Netflix Eureka, Spring Cloud Gateway, OpenFeign, and Kafka!

## Architecture Diagram & Details
Please read the comprehensive architectural breakdown at [docs/architecture.md](docs/architecture.md) to understand how the components interplay via synchronous HTTP Feign queries & asynchronous Kafka events!

## How to Boot Up the System (The Professional Way)

You do NOT need to run these services individually across 5 different integrated terminals. We use **Docker Compose**.

1. Ensure **Docker Desktop** is running.
2. Open a terminal directly at the root (`C:\BANK PROJECT`).
3. Compile all the Microservices simultaneously using the centralized Gradle Wrapper:
   ```bash
   .\gradlew.bat clean build -x test
   ```
4. Step inside the `docker` directory and fire up the cluster:
   ```bash
   cd docker
   docker-compose up --build -d
   ```
> Look alive—this boots exactly 9 heavy containers (Zookeeper, Kafka, MySQL, Eureka, API Gateway, and 4 Custom Spring Boot Services). Eureka takes about ~45 seconds to fully register all health-pins. 

---

## Testing API Flow (Via Gateway Port 8080)

Because we use a massive Edge Proxy (Gateway), all requests route cleanly into `localhost:8080`.

### 1. Register User (User Service)
```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\`"name\`":\`"Super\`", \`"email\`":\`"super@example.com\`", \`"password\`":\`"password123\`"}"
```

### 2. Login & Validate JWT (Gateway filter)
```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\`"email\`":\`"super@example.com\`", \`"password\`":\`"password123\`"}"
```
Copy your JWT Token output. Substitute it as `<TOKEN>` below.

### 3. Create Source & Destination Accounts
```bash
# Create Account 1
curl -X POST http://localhost:8080/api/accounts/user/1 -H "Authorization: Bearer <TOKEN>"

# Create Account 2
curl -X POST http://localhost:8080/api/accounts/user/1 -H "Authorization: Bearer <TOKEN>"
```

### 4. Direct Operations
```bash
# Deposit $500 into Account 1
curl -X POST "http://localhost:8080/api/accounts/1/deposit?amount=500" -H "Authorization: Bearer <TOKEN>"
```

### 5. OpenFeign & Kafka Orchestration (Transaction Service)
```bash
# Transfer $150 from Account 1 to Account 2
curl -X POST http://localhost:8080/api/transactions/transfer \
-H "Authorization: Bearer <TOKEN>" \
-H "Content-Type: application/json" \
-d "{\`"sourceAccountId\`": 1, \`"destinationAccountId\`": 2, \`"amount\`": 150.00}"
```
> **Magic moment**: Wait 2 seconds and check your Docker console for `notification-service`. The Transaction Service synchronously ordered the Account Service to shift the balances via OpenFeign, and then blasted out a Kafka event that the Notification Service instantly consumed and logged!

Congratulations on leveling up to Senior Cloud Architect!
