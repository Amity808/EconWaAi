# 02 — Technology Stack

## 1. Architectural Philosophy

EconWaAi powers mission-critical B2B commerce where money, goods, and livelihoods are at stake. The tech stack is chosen to meet four strict criteria:
1. **Financial Precision & Banking Interoperability**: Seamless compatibility with CBN-licensed partner MFBs, NIBSS, and enterprise Nigerian banking infrastructure.
2. **High-Throughput Concurrency**: Handle massive morning traffic spikes (06:00 to 14:00 WAT) across thousands of WhatsApp voice webhooks and live quotes.
3. **Bandwidth-First Mobile Web**: Driver and field pages must load under 200 KB on low-cost Android smartphones.
4. **Vast Local Engineering Talent**: Fast hiring, onboarding, and team scaling in the Nigerian tech ecosystem.

---

## 2. Core Backend: Java 21 (LTS) + Spring Boot 3.3+

### 2.1 Why Java 21?
- **Enterprise Fintech Standard**: Nearly all commercial banks, partner MFBs, payment switches (Interswitch, Remita), and NIBSS integrations in Nigeria run on Java. This guarantees out-of-the-box compatibility with banking SDKs, ISO 8583 protocols, and MT940 bank statement formats.
- **Virtual Threads (Project Loom)**: By enabling `spring.threads.virtual.enabled=true`, Java 21 allocates lightweight, user-mode virtual threads per incoming request. A single instance effortlessly handles tens of thousands of concurrent WhatsApp webhooks and real-time quotes without reactive programming complexity.
- **Sealed Interfaces & Pattern Matching**: Java 21 supports algebraic data types, allowing strict compile-time exhaustiveness checks for multi-party financial state machines:
  ```java
  public sealed interface OrderState 
      permits Created, PendingReconfirmation, AwaitingPayment, PaidEscrow, Dispatched, Delivered, Disputed {}
  ```
- **Records**: Concise, immutable domain models and data transfer objects (`public record QuotationDto(...)`).
- **Talent Pool & Maintainability**: Extensive senior engineering talent pool in Lagos and across West Africa, making team hiring, retention, and maintenance straightforward.

### 2.2 Why Spring Boot 3.3+?
- **Native Spring Security**: Enterprise-grade JWT authentication, granular Role-Based Access Control (RBAC), and method-level authorization.
- **jOOQ / Spring Data + Flyway**: Type-safe, compile-time verified SQL queries and repeatable database migrations.
- **RestClient & WebClient**: Built-in HTTP/2 client for high-speed, mTLS-secured communication with Partner MFB core banking APIs and Meta Graph APIs.
- **GraalVM Native Image Ready**: Optional ahead-of-time (AOT) compilation to standalone binaries with sub-50ms startup times and reduced memory footprints.
- **Built-in Production Observability**: Micrometer, Prometheus actuator metrics, and OpenTelemetry distributed tracing.

### 2.3 Key Java / Maven Dependencies

| Dependency | Purpose | Version |
|:---|:---|:---|
| `spring-boot-starter-web` | REST API layer and embedded high-concurrency Tomcat | 3.3.x |
| `spring-boot-starter-security` | Stateless JWT authentication, CORS, and endpoint security | 3.3.x |
| `spring-boot-starter-data-jpa` | ORM, connection pooling (HikariCP), transaction management | 3.3.x |
| `jooq-spring-boot-starter` | Type-safe, fluent SQL query construction for complex reporting | 3.19.x |
| `flyway-core` | Automated, version-controlled PostgreSQL schema migrations | 10.x |
| `spring-boot-starter-data-redis` | Redis Lettuce client for session caching and webhook queues | 3.3.x |
| `spring-boot-starter-validation` | Declarative Bean Validation (JSR 380) for incoming payloads | 3.3.x |
| `io.jsonwebtoken:jjwt` | Fast, secure JWT encoding, signing, and verification | 0.12.x |
| `com.bucket4j:bucket4j-core` | In-memory and distributed token-bucket rate limiting | 8.x |
| `org.springdoc:springdoc-openapi` | Automated OpenAPI 3.0 (Swagger) interactive documentation | 2.5.x |
| `micrometer-registry-prometheus` | Production metrics exporter for Prometheus / Grafana | Latest |

---

## 3. AI Speech-to-Text & NLU Microservice: Python 3.11 + FastAPI

### 3.1 Service Purpose
While the core business logic, finance, and logistics run on Java, audio preprocessing and neural Whisper speech recognition leverage Python’s specialized AI ecosystem. The AI service runs as a dedicated private internal microservice accessed by the Java Spring Boot backend over high-speed HTTP/2.

### 3.2 Stack Components

| Component | Library / Model | Purpose |
|:---|:---|:---|
| **Web Layer** | `FastAPI` + `Uvicorn` | Asynchronous API handling audio ingestion and entity extraction |
| **STT Engine** | `Faster-Whisper` (CTranslate2) | 4x faster Whisper inference with 8-bit quantization |
| **Speech Adaptation** | Fine-tuned acoustic adapter | Optimized for Nigerian Pidgin, Yoruba, Hausa, Igbo accents |
| **Audio Processing** | `ffmpeg` / `soundfile` | Resampling WhatsApp audio notes (.ogg/Opus) to 16kHz WAV |
| **Entity Extraction** | `spacy` + `regex` | Extracts commodity, quantity, and local trade units (*mudu*, *derica*) |
| **Embedding Similarity**| `sentence-transformers` | Maps dialectal trade terms to canonical catalog items |

---

## 4. Primary Persistence: PostgreSQL 16

### 4.1 Key Capabilities Used
- **Row-Level Security (RLS)**: Enforces multi-tenant data boundaries across organizations (buyers, suppliers, transporters) at the database engine level.
- **Trigram Fuzzy Search (`pg_trgm`)**: Powers phonetic and typo-tolerant search across commodity names, brand synonyms, and market landmarks.
- **JSONB**: Retains raw webhook payloads, audit logs, and flexible configuration without schema bloat.
- **ACID Transactions**: Absolute consistency required for ledger operations, credit balances, and virtual account reconciliation.

---

## 5. In-Memory & Queues: Redis 7

- **WhatsApp Session State**: Caches multi-step conversation context (current step in RFQ, quote review, or delivery flow).
- **Webhook Deduplication**: Prevents duplicate processing of bank and WhatsApp webhooks using atomic `SET NX EX`.
- **Audio Job Queue**: Distributes incoming voice notes to AI inference workers.
- **Rate Limiting**: Sliding-window rate limiting per IP and phone number to protect public endpoints.

---

## 6. Frontend & Mobile Clients

### 6.1 Supplier Portal & Admin Backoffice: Next.js 15
- **Next.js 15 App Router**: Server-Side Rendering (SSR) for initial load speed and React Server Components (RSC) for reduced client bundle.
- **Tailwind CSS**: Utility-first styling with custom design tokens for responsive desktop and tablet workflows.
- **TanStack Query (React Query)**: Optimistic UI updates, caching, and background data synchronization.

### 6.2 Driver Field Web Client: Ultra-Lightweight PWA (<200KB)
- **Zero Heavy Frameworks**: Handcrafted micro-bundle (Preact / Vanilla JS + Tailwind JIT) targeting < 200 KB initial payload.
- **Offline Storage**: Uses browser `IndexedDB` to queue proof-of-delivery photos and status transitions when connectivity drops.
- **No App Store Friction**: Delivered via instant SMS/WhatsApp magic link; no Google Play or Apple App Store download required.

---

## 7. Storage, Media & Edge Infrastructure

- **Cloudflare Edge**: Global CDN, TLS 1.3 termination, Web Application Firewall (WAF), and DDoS protection.
- **Object Storage**: S3-compatible storage (Cloudflare R2) with zero egress fees for proof-of-delivery photos, invoice PDFs, and voice recordings.
- **Containerization**: Multi-stage Docker builds producing optimized Eclipse Temurin OpenJDK 21 distroless containers and optimized CUDA/CPU Python containers.
