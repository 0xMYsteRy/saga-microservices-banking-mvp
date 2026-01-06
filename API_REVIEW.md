# API & Saga Design Review for saga-microservices-banking-mvp

## Saga Orchestrator Service

### REST APIs

| Method | Path | Purpose / Flow Role | Input / Output | Idempotency & Error Handling |
| --- | --- | --- | --- | --- |
| GET | `/api/saga/instances` | Admin insight into all saga instances (user onboarding, payment) | Returns `List<SagaInstance>` (internal entity schema) | No pagination; always 200 even when empty. Admin-only via `@PreAuthorize`. |
| POST | `/api/saga/start/user-onboarding` | Starts user onboarding saga by emitting `CreateUserCommand` | No body; builds `User` payload from JWT claims (username, fullName, email). 202 with text message. | No validation or idempotency; duplicate calls spawn multiple sagas. Always returns 202 even when saga immediately fails. |
| POST | `/api/saga/start/payment-processing` | Starts payment saga by emitting `ValidatePaymentCommand` | Body: `PaymentRequest` (source/dest account numbers, amount, description). 202 with saga ID text. | Same gaps—no dedupe, no error differentiation, no JSON response.

### Message APIs & Saga Roles

- **Commands emitted**: `createUserCommand`, `accountOpenCommand`, `deleteUserCommand`, `validatePaymentCommand`, `processPaymentCommand`, `recordTransactionCommand`, `updatePaymentStatusCommand`, `sendNotificationCommand`.
- **Events consumed**: `UserCreatedEvent`, `UserCreationFailedEvent`, `AccountOpenedEvent`, `AccountOpenFailedEvent`, `UserDeletedEvent`, `PaymentValidatedEvent`, `PaymentValidationFailedEvent`, `PaymentProcessedEvent`, `PaymentFailedEvent`, `TransactionRecordedEvent`, `TransactionFailedEvent`, `PaymentStatusUpdatedEvent`.
- **Observability**: rich logging but correlation IDs not propagated from Kafka headers.

**Risks**: No idempotency keys or dedupe; commands reuse domain entities (tight coupling). Saga start endpoints always accept, lacking synchronous validation. No `/api/saga/instances/{id}`.

**Improvements**:
- Require `Idempotency-Key` on saga start endpoints; respond with structured JSON `{ sagaId, statusUrl }`.
- Add `/api/saga/instances/{id}` and query filtering for monitoring.
- Introduce event envelope (eventId, version, correlationId) and persist processed command IDs.
- Fail fast: validate payment payloads up front and respond 400 if invalid instead of starting saga.

---

## User Service

### REST APIs

| Method | Path | Purpose | Input / Output | Observations |
| --- | --- | --- | --- | --- |
| PUT | `/api/users/{id}` | Admin updates user profile | `UpdateUserRequest` -> `User` entity | Returns entity directly; runtime exception for missing user triggers 500. No idempotent patch semantics. |
| GET | `/api/users` | Admin listing | `List<User>` | No pagination or filtering; leaks fields. |
| GET | `/api/users/me` | Current user profile | None -> `User` | Throws when user absent (`RuntimeException`). |

### Message APIs

- Consumes `CreateUserCommand`, `DeleteUserCommand`.
- Emits `UserCreatedEvent`, `UserCreationFailedEvent`, `UserDeletedEvent`, `UserDeletionFailedEvent`.
- Acts as first step and compensating action for onboarding saga.

**Risks**: REST endpoints leak internal entity structure; missing 404 handling. Message consumers have no dedupe; duplicate `CreateUserCommand` creates duplicate users; compensation `DeleteUserCommand` may delete legitimate user if reprocessed.

**Improvements**:
- Introduce DTOs (no password fields) and standard error responses.
- Track processed command IDs; ignore duplicates.
- Provide `/api/users/{id}` for completeness with proper auth checks.

---

## Account Service

### REST APIs

| Method | Path | Purpose | Input / Output | Issues |
| --- | --- | --- | --- | --- |
| GET | `/api/accounts` | Admin list | List of `Account` | No pagination. |
| GET | `/api/accounts/{accountNumber}` | Admin or account holder fetch | `Account` | Ownership not enforced—any account holder can fetch others’ accounts. |
| POST | `/api/accounts` | Admin manual creation | `Account` body | No validation or idempotency. |
| GET | `/api/accounts/my-accounts` | Authenticated user accounts | List | Works but no caching or filters. |

### Message APIs

- Consumes `OpenAccountCommand`, `ProcessPaymentCommand`.
- Emits `AccountOpenedEvent`, `AccountOpenFailedEvent`, `PaymentProcessedEvent`, `PaymentFailedEvent`.
- Transfers funds via `accountService.transferMoney` (atomicity depends on DB transactions).

**Risks**: Duplicate `ProcessPaymentCommand` could double debit; no commandId tracking. Payment failure reasons are string-only.

**Improvements**:
- Enforce owner check on `/accounts/{accountNumber}`.
- Add idempotent handling keyed by `commandId` for both account creation and transfers.
- Emit structured error codes.

---

## Payment Service

### REST APIs

| Method | Path | Purpose | Input / Output | Issues |
| --- | --- | --- | --- | --- |
| GET | `/api/payments` | Admin list | List | No filtering. |
| GET | `/api/payments/my-payments` | Account-holder history | List | No pagination. |
| GET | `/api/payments/{id}` | Admin/holder view | `Payment` | Ownership not enforced. |
| POST | `/api/payments` | Create payment record | `Payment` entity | Allows direct DB writes outside saga; no idempotency. |

### Message APIs

- Consumes `ValidatePaymentCommand` → emits `PaymentValidatedEvent` or `PaymentValidationFailedEvent` (also creates payment row).
- Consumes `UpdatePaymentStatusCommand` → emits `PaymentStatusUpdatedEvent` (no failure event).

**Risks**: Duplicate validation commands create duplicate DB rows. Update command lacks failure reporting; saga could hang. REST endpoint bypasses saga.

**Improvements**:
- Make REST POST forward to saga start and return saga ID instead of writing DB.
- Persist `commandId` to enforce idempotency.
- Emit `PaymentStatusUpdateFailedEvent` when update fails.

---

## Transaction Service

### REST APIs

| Method | Path | Purpose | Notes |
| --- | --- | --- | --- |
| GET `/api/transactions` | Admin list | No pagination. |
| GET `/api/transactions/{id}` | Admin/holder fetch | Ownership not enforced. |
| GET `/api/transactions/my-transactions` | User history | No filters. |

### Message APIs

- Consumes `RecordTransactionCommand` (creates two transaction rows) → emits `TransactionRecordedEvent` or `TransactionFailedEvent`.

**Risks**: Duplicate command duplicates ledger records. No unique constraint on `reference`. No attribute to trace to payment ID besides string.

**Improvements**:
- Use unique constraint (paymentId + type) and reject duplicates.
- Add query params for time range/paging.

---

## Notification Service

### REST APIs

| Method | Path | Purpose | Notes |
| --- | --- | --- | --- |
| GET `/api/notifications` | Admin list | Secured. |
| GET `/api/notifications/{id}` | Fetch by ID | Missing `@PreAuthorize` (public). |
| GET `/api/notifications/my-notifications` | User inbox | Missing `@PreAuthorize`, so unauthenticated access possible. |

### Message APIs

- Consumes `SendNotificationCommand`; no success/failure events emitted.

**Risks**: Data leakage due to missing auth. Saga can’t detect notification failures.

**Improvements**:
- Add `@PreAuthorize("isAuthenticated()")` to `/my-notifications` and ownership guard on `/notifications/{id}`.
- Emit `NotificationSentEvent` / `NotificationFailedEvent` for observability.

---

## Cross-Cutting Gaps & Recommendations

- **Idempotency**: No REST or message handler implements dedupe. Add command-processing tables keyed by `commandId` and require `Idempotency-Key` headers for POSTs.
- **Security**: Enforce ownership checks for account/payment/transaction/notification reads. Document role boundaries.
- **Observability**: Leverage `CorrelationIdMessageUtils` for all outgoing messages, propagate MDC entries, integrate OpenTelemetry tracing.
- **DTO Layer**: Replace entity exposure with versioned DTOs and standard error payloads.
- **Event Versioning**: Wrap event payloads in envelopes with version numbers and correlation IDs to enable backward compatibility.
- **Saga Monitoring**: Provide per-saga REST endpoints (`/api/saga/instances/{id}`) and status webhooks.
- **Readme Scope**: Keep README focused on architecture; move this detailed API review to `API_REVIEW.md` (this file).

