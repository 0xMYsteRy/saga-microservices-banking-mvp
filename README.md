# Banking as a Service Platform

A saga-orchestrated banking platform implemented as a Spring Boot microservice suite. The goal of this README is to highlight the high-level architecture and explain how the saga pattern is applied to mission-critical banking workflows.

## High-Level Architecture

```mermaid
graph TD
    subgraph Client
        UI[Web / API Clients]
    end

    UI --> APIGateway

    subgraph Gateway
        APIGateway[API Gateway]
    end

    subgraph Core Services
        UserService[User Service]
        AccountService[Account Service]
        PaymentService[Payment Service]
        TransactionService[Transaction Service]
        NotificationService[Notification Service]
        SagaOrchestrator[Saga Orchestrator Service]
    end

    subgraph Infrastructure
        Eureka[Eureka Service Discovery]
        Kafka[Kafka Broker]
        MySQL[(MySQL Databases)]
        Keycloak[Keycloak Auth]
    end

    APIGateway -->|discovery| Eureka
    APIGateway -->|auth| Keycloak
    UserService --> MySQL
    AccountService --> MySQL
    PaymentService --> MySQL
    TransactionService --> MySQL
    NotificationService --> MySQL

    SagaOrchestrator --> Kafka
    UserService --> Kafka
    AccountService --> Kafka
    PaymentService --> Kafka
    TransactionService --> Kafka
    NotificationService --> Kafka
```

### Technology Stack
- **Spring Boot 3 / Java 21** for each microservice
- **Maven** multi-module build with a shared `common-lib`
- **Spring Cloud** components for Gateway and Eureka discovery
- **MySQL 8** per service schema
- **Apache Kafka + Spring Cloud Stream** for event-driven communication
- **Keycloak** for OAuth2 / OpenID Connect authentication
- **Docker Compose** for local orchestration

### Repository Layout

| Path | Description |
| --- | --- |
| `api-gateway/` | Spring Cloud Gateway entry point with routing, auth and admin aggregation |
| `service-discovery/` | Eureka server for service registry and health monitoring |
| `saga-orchestrator-service/` | Central orchestrator implementing abstract saga logic |
| `user-service/` | User onboarding, roles, and credential management |
| `account-service/` | Account lifecycle, balance checks, and payment validation |
| `payment-service/` | Payment lifecycle, status management, compensation triggers |
| `transaction-service/` | Ledger entries and transaction history |
| `notification-service/` | Event-driven email notifications |
| `common-lib/` | Shared entities, events, commands, and saga abstractions |
| `config/` | Centralized configuration files |
| `docker-compose.yml` | Local deployment of infra + services |

## Saga Pattern Design

The platform uses an **abstract saga orchestration framework** housed in `common-lib` and executed by `saga-orchestrator-service`. Key concepts:

- **Saga Instance** – State persisted per workflow with lifecycle states (`STARTED`, `IN_PROGRESS`, `COMPLETED`, `FAILED`).
- **Saga Steps** – Declarative steps mapped to service commands and compensations.
- **Events & Commands** – Typed messages published via Kafka topics for each step transition.
- **Compensation Logic** – Automatic rollback functions attached to each step to maintain consistency.

### Lifecycle
1. **Start** – Client request hits API Gateway -> orchestrator creates saga instance and emits first command.
2. **Progress** – Services consume commands, execute business logic, and emit domain events on success/failure.
3. **Transition** – Orchestrator listens for events, advances to next step or triggers compensation.
4. **Completion** – Final state recorded and notification service informs stakeholders.

### User Onboarding Saga

```mermaid
graph LR
    A[Start Saga] --> B[Create User]
    B --> C{Role}
    C -->|Admin| D[Send Admin Notification]
    C -->|Account Holder| E[Create Account]
    E --> F[Send Welcome Notification]
    E -->|failure| G[Delete User / Compensation]
    D --> H[Saga Complete]
    F --> H
    G --> I[Saga Failed]
```

- **Create User** (`user-service`) persists the profile and emits `UserCreated`.
- **Account Creation** (`account-service`) opens default account for non-admin users and emits `AccountCreated` or `AccountCreationFailed`.
- **Notifications** (`notification-service`) email confirmations via `NotificationRequested` events.
- **Compensation** – If account creation fails, orchestrator triggers `DeleteUserCommand` to rollback the initial step.

### Payment Processing Saga

```mermaid
graph LR
    P1[Payment Requested] --> P2[Validate Account & Balance]
    P2 -->|ok| P3[Process Payment]
    P3 --> P4[Record Transaction]
    P4 --> P5[Update Payment Status]
    P5 --> P6[Send Notification]
    P6 --> P7[Saga Complete]
    P2 -->|fail| PF1[Saga Failed]
    P3 -->|fail| PF2[Mark Payment Failed]
    P4 -->|fail| PF3[Compensate Payment]
```

- **Validation** (`account-service`) ensures sufficient funds; emits failure to short-circuit.
- **Processing** (`payment-service`) reserves/debits funds and emits `PaymentProcessed` or `PaymentFailed`.
- **Transaction Recording** (`transaction-service`) generates ledger entries and ensures idempotency.
- **Status + Notification** – Payment service finalizes status; notification service informs user.
- **Compensation** – On downstream failure, orchestrator issues compensating commands (refund, status update, send failure email).

### Messaging Contracts
- Topics follow the pattern `<service>.<domain>.<event>`.
- Messages contain correlation IDs and saga instance IDs for traceability.
- Each service implements both command handlers and event emitters using Spring Cloud Stream bindings.
- OpenTelemetry tracing is enabled across services to join log spans with saga IDs.

## Running the Platform

1. **Start everything via Docker Compose**
   ```bash
   ./start-docker-compse.sh
   ```
   This script stops existing containers, builds all services with Maven, and launches the full stack.

2. **Manual mode** (infrastructure first, services locally):
   ```bash
   ./start-infra.sh
   ./start-springboot-services.sh
   ```

3. **Endpoints**
   - API Gateway: `http://localhost:8080`
   - Service Discovery Dashboard: `http://localhost:8761`
   - Keycloak: `http://localhost:8180`

## Observability
- OpenTelemetry Java agent attached to each service prints trace/span IDs in logs.
- Saga orchestrator exposes `/api/saga/instances` for real-time monitoring (admin-only).
- Docker logs can be tailed per service: `docker-compose logs -f <service>`.

## Next Steps
- Extend saga definitions in `saga-orchestrator-service` to cover new workflows (e.g., loan applications).
