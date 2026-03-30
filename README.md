# Digital Banking Backend (Spring Boot Microservices)

Welcome to the Digital Banking Backend project! This project demonstrates building a scalable microservices architecture from scratch using Java 21, Spring Boot, Spring Cloud, Kafka, Docker, and MySQL.

## Phases Implemented

*   **Phase 1: Basic Microservices (User & Account)**
    *   Created `user-service` and `account-service` with MySQL DBs.
*   **Phase 2: JWT Authentication**
    *   Secured `user-service` with BCrypt and `jjwt` tokens.
*   **Phase 3: API Gateway**
    *   Created `api-gateway` as a single point of entry. Routes all traffic automatically.
*   **Phase 4: Service Discovery**
    *   Created `eureka-server`. Services dynamically register with it.
*   **Phase 5: Kafka Event-Driven Communication**
    *   When a user registers across the Gateway, `user-service` publishes a `userRegistrationTopic` event to a local Kafka Broker.
    *   `account-service` consumes this event and *automatically* creates a banking account for the new user!
*   **Phase 6: Dockerization**
    *   Added Dockerfiles for all microservices.
    *   Added a massive `docker-compose.yml` to spin up Zookeeper, Kafka, MySQL, Eureka, API Gateway, User Service, and Account Service all in one go, networked together!

---

## How to Run the Project (The Easy Way)

Make sure you have **Docker Desktop** installed and running.

1. Open a terminal at the root of the project.
2. Build the `.jar` files for all services first:
```bash
cd api-gateway && ./gradlew.bat build -x test && cd ..
cd eureka-server && ./gradlew.bat build -x test && cd ..
cd user-service && ./gradlew.bat build -x test && cd ..
cd account-service && ./gradlew.bat build -x test && cd ..
```

3. Run the entire cluster using Docker Compose!
```bash
docker-compose up --build -d
```
> This will start **7 interconnected containers**. It might take a minute or two for MySQL to initialize and Eureka to register the services.

---

## API Testing (Postman / CURL Commands)

*Important: Since we use the API Gateway on port 8080, we ONLY send requests to `localhost:8080`.*

### 1. Register a New User (This triggers Kafka!)
```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\`"name\`":\`"Super\`", \`"email\`":\`"super@example.com\`", \`"password\`":\`"password123\`"}"
```
> **Magic moment**: Wait 2 seconds. The `user-service` just sent an event to Kafka. The `account-service` consumed it and automatically created an account for this user!

### 2. Login to get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\`"email\`":\`"super@example.com\`", \`"password\`":\`"password123\`"}"
```
*(Copy the long text output. This is your JWT Token. We will refer to it as `<YOUR_TOKEN>` below).*

### 3. View the Auto-Created Account Details
Because Kafka did its job, the account already exists! Let's fetch it from the Account Service.
```bash
curl -X GET http://localhost:8080/api/accounts/user/1
```

### 4. Deposit Money
```bash
curl -X POST "http://localhost:8080/api/accounts/1/deposit?amount=500"
```

Congratulations! You have successfully built a complete Digital Banking Backend with Microservices, Security, Routing, Discovery, Messaging, and Docker!
