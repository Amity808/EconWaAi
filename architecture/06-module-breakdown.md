# 06 — Module Breakdown (Maven Multi-Module Modular Monolith)

## 1. Architectural Strategy: Spring Boot Modular Monolith

EconWaAi is architected as a **Java 21 / Spring Boot 3.3+ Modular Monolith** organized as a Maven multi-module project. This architecture combines deployment simplicity with strict compile-time boundaries enforced by Maven module dependencies:

```
econwa-platform/
├── pom.xml                   # Parent POM: dependencyManagement, Java 21 compiler, Loom flags
├── econwa-common             # Value objects, Money (Kobo), Base Exceptions, Enums
├── econwa-domain             # Sealed domain interfaces, Domain Models, Repository Contracts
├── econwa-infrastructure     # Spring Data JPA, jOOQ, RLS Interceptors, Redis, Flyway migrations
├── econwa-ai-client          # HTTP/2 RestClient for Python Whisper STT/NER microservice
├── econwa-catalog            # Canonical commodities, Registered Supplier Catalogues, Units
├── econwa-rfq                # RFQ services, Supplier Quoting, Price Validity Expiration Watcher
├── econwa-orders             # Order state machine, Step 9 Reconfirmation, Escrow Coordinator
├── econwa-logistics          # Landmark Geocoder, Driver Offline Queue Sync, Exception Triager
├── econwa-mfb                # Partner MFB Core Banking Client, Virtual NUBAN Allocator, Webhooks
├── econwa-credit             # Trade Score Calculator, Points Ledger, BNPL Underwriter
├── econwa-messaging          # WhatsApp Cloud API, Template Cost Auditor, Termii Fallback
├── econwa-finance            # Daily Bank Statement Reconciliation, Invoicing, Tax Profiles
└── econwa-api-app            # Spring Boot Application entrypoint, @RestControllers, Security
```

---

## 2. Maven Module Dependency Graph

```
                               ┌────────────────┐
                               │ econwa-api-app │
                               └───────┬────────┘
                                       │
         ┌──────────────┬──────────────┼──────────────┬──────────────┐
         ▼              ▼              ▼              ▼              ▼
  ┌─────────────┐┌─────────────┐┌────────────┐┌─────────────┐┌──────────────┐
  │ econwa-rfq  ││econwa-orders││econwa-logis││ econwa-mfb  ││econwa-credit │
  └──────┬──────┘└──────┬──────┘└──────┬─────┘└──────┬──────┘└──────┬───────┘
         │              │              │             │              │
         └──────────────┼──────────────┼─────────────┴──────────────┘
                        ▼              ▼
                 ┌─────────────┐┌──────────────┐
                 │econwa-ai-cl ││econwa-message│
                 └──────┬──────┘└──────┬───────┘
                        │              │
                        ▼              ▼
                 ┌─────────────────────────────┐
                 │    econwa-infrastructure    │
                 └──────────────┬──────────────┘
                                │
                                ▼
                 ┌─────────────────────────────┐
                 │        econwa-domain        │
                 └──────────────┬──────────────┘
                                │
                                ▼
                 ┌─────────────────────────────┐
                 │        econwa-common        │
                 └─────────────────────────────┘
```

---

## 3. Module Responsibilities & Packages

### 3.1 `econwa-common`
- **Zero Heavy Frameworks**: Pure Java 21 value objects and utilities.
- **`Kobo` Record**: Strongly typed `public record Kobo(long amount)` preventing floating-point math errors.
- **Tenant Context**: `OrgContext` holder with `orgId`, `orgType`, `userId`, and `role`.

### 3.2 `econwa-domain`
- **Domain Models & Enums**: Pure domain entities without framework pollution.
- **Sealed State Machines**:
  ```java
  public sealed interface OrderState 
      permits Created, PendingReconfirmation, AwaitingPayment, PaidEscrow, Dispatched, Delivered, Disputed {}
  ```
- **Repository Interfaces**: Clean domain interfaces (`OrderRepository`, `QuotationRepository`, `SupplierCatalogRepository`) implemented in infrastructure.

### 3.3 `econwa-infrastructure`
- **PostgreSQL & Flyway**: Manages database migrations and connection pooling via HikariCP.
- **Tenant RLS Interceptor**: Spring AOP / Hibernate connection interceptor that issues `SET LOCAL app.current_org_id` on every connection checkout.
- **jOOQ Code Generation**: Type-safe SQL query generation for multi-table analytics and reporting.

### 3.4 `econwa-ai-client`
- **Spring `RestClient`**: Communicates with the Python Faster-Whisper microservice over HTTP/2.
- **Speech Audio Gateway**: Buffers WhatsApp audio notes and initiates async transcription tasks.

### 3.5 `econwa-catalog` & `econwa-rfq`
- **Registered Supplier Catalogues**: Manages supplier-specific SKUs, trade units (*50kg bag*, *mudu*, *carton*), live stock statuses, and bulk spreadsheet ingestion.
- **Quotation Validity Monitor**: Scheduled task using Virtual Threads checking `valid_until` timestamps to mark expired quotes.

### 3.6 `econwa-orders`
- **Step 9 Reconfirmation Service**: Coordinates supplier price lock and buyer re-approval workflows.
- **Escrow State Machine**: Coordinates payment transitions and triggers logistics dispatch upon MFB webhook confirmation.

### 3.7 `econwa-logistics`
- **Landmark Geocoder**: Resolves descriptive landmark directions and market lines into driver delivery waypoints.
- **Driver Offline Queue Processor**: Ingests batched mobile events, verifying device capture timestamps and GPS drift.

### 3.8 `econwa-mfb`
- **Virtual NUBAN Engine**: Interfaces with Partner MFB Core Banking API to dynamically generate dedicated virtual accounts per order.
- **Webhook Verifier**: Computes and validates HMAC-SHA256 signatures on inbound bank settlement callbacks.

### 3.9 `econwa-credit`
- **Trade Score Engine**: Evaluates 90-day order volume, payment timeliness, and dispute frequency.
- **BNPL Restocking Controller**: Enforces direct supplier disbursement for all credit purchases.

### 3.10 `econwa-messaging`
- **Meta Cloud API Client**: Sends structured WhatsApp templates, interactive buttons, and session replies.
- **Cost Auditor**: Tracks per-template cost in `message_costs` to safeguard platform margins.

### 3.11 `econwa-finance`
- **Daily Reconciliation Job**: Scheduled midnight task comparing MFB core statement dumps with platform records, raising discrepancy alerts.
- **Tax & Invoice Service**: Generates separated PDFs (Supplier Goods Invoice, Logistics Bill, Platform Service Charge).

---

## 4. Internal Event Bus & Asynchronous Decoupling

Subsystems communicate asynchronously using **Spring `ApplicationEventPublisher`** (backed by Virtual Threads) and **Redis Streams**:
- `OrderStep9ReconfirmedEvent` ──▶ `econwa-mfb` (Requests dedicated virtual NUBAN).
- `MfbPaymentSettledEvent` ──▶ `econwa-orders` (Transitions order to `PaidEscrow`), `econwa-messaging` (Notifies parties).
- `DeliveryHandoverCompletedEvent` ──▶ `econwa-orders` (Completes order), `econwa-credit` (Awards trade points), `econwa-mfb` (Triggers supplier payout).
