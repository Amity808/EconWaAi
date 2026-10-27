# 08 — Infrastructure & Deployment

## 1. Cloud Architecture & Edge Topology

Network latency and packet drop rates on West African mobile networks dictate an **edge-accelerated infrastructure**:

```
                                  ┌─────────────────────────────┐
                                  │   Cloudflare Global Edge    │
                                  │   (Lagos & Abuja PoPs)      │
                                  └──────────────┬──────────────┘
                                                 │
                                                 │ TLS 1.3 / HTTP/2
                                                 ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             PRIMARY CLOUD REGION                                 │
│                                                                                  │
│   ┌──────────────────────────────────────────────────────────────────────────┐   │
│   │                      INDEPENDENT LOAD BALANCER / INGRESS                 │   │
│   │                      (Nginx / Envoy with Rate Limiting)                  │   │
│   └──────────────────────┬───────────────────────────────────┬───────────────┘   │
│                          │                                   │                   │
│                          ▼                                   ▼                   │
│   ┌─────────────────────────────┐             ┌─────────────────────────────┐    │
│   │   Core Backend Containers   │             │   AI Inference Workers      │    │
│   │   (Java 21 Spring Boot 3)   │             │   (Python Faster-Whisper)   │    │
│   └──────────────┬──────────────┘             └──────────────┬──────────────┘    │
│                  │                                           │                   │
│                  │ SQL / Redis Protocol                      │ Job Pull          │
│                  ▼                                           ▼                   │
│   ┌─────────────────────────────┐             ┌─────────────────────────────┐    │
│   │   PostgreSQL 16 Cluster     │             │   Redis 7 Cluster           │    │
│   │   (Primary + Read Replica)  │             │   (Queues, Sessions, Cache) │    │
│   └─────────────────────────────┘             └─────────────────────────────┘    │
│                                                                                  │
│   ┌──────────────────────────────────────────────────────────────────────────┐   │
│   │                     OBJECT STORAGE (Cloudflare R2)                       │   │
│   │       • Voice Notes (.ogg)   • POD Photos   • Generated Invoices (PDF)   │   │
│   └──────────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Audio Processing Pipeline

Processing high volumes of WhatsApp voice notes requires an asynchronous, fault-tolerant media pipeline:

```
[WhatsApp Audio Webhook]
          │
          ▼
[Spring Boot Webhook Receiver] ── Returns 200 OK immediately to Meta (<200ms)
          │
          ▼
[Stream Audio to Cloudflare R2] ── Generates private signed URL
          │
          ▼
[Push Job to Redis Queue `audio_stt_jobs`]
          │
          ▼
[Python Faster-Whisper Worker] ── Pulls job, runs 16kHz resample & STT
          │
          ▼
[Entity Extraction & NER] ── Maps trade units and quantities
          │
          ▼
[Post to Spring Boot Internal Callback / Push WebSocket update to Buyer WhatsApp]
```

---

## 3. Containerization Strategy

### 3.1 Java Spring Boot Backend Container
```dockerfile
# Multi-stage build producing an optimized Eclipse Temurin OpenJDK 21 container
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=builder /app/econwa-api-app/target/*.jar app.jar
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
```

### 3.2 Python AI Inference Container
- Uses NVIDIA CUDA base image where GPU instances are provisioned, or CPU-optimized AVX512 binaries for cost-effective CPU inference.
- Quantized model weights pre-baked into container image to eliminate startup download delays.

---

## 4. Disaster Recovery, Backups & SLA

1. **Database Snapshots**: Continuous WAL (Write-Ahead Log) archiving with Point-in-Time Recovery (PITR) up to 30 days.
2. **Cold Storage Sync**: Nightly automated encrypted database dumps pushed to multi-region offsite storage.
3. **Daily Bank Reconciliation Audit**: Every midnight (23:59 Lagos Time), background cron fetches the official settled statement dump from the Partner MFB and performs an automated line-by-line reconciliation against the `bank_transactions` ledger. Discrepancies generate P1 alerts to the engineering and finance on-call rotations.
