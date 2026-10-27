# 11 — Load Balancing, Ingress & Regional Routing

## 1. Edge & Ingress Topology

Traffic across Nigerian B2B commerce experiences sharp diurnal spikes: heavy trading occurs between 06:00 and 14:00 West Africa Time (WAT). The ingress architecture must handle massive webhook bursts and poor mobile network conditions:

```
                                  ┌─────────────────────────────┐
                                  │   Cloudflare Global Edge    │
                                  │   • TLS 1.3 Termination     │
                                  │   • Lagos & Abuja PoPs      │
                                  │   • DDoS & WAF Scrubbing    │
                                  └──────────────┬──────────────┘
                                                 │
                                                 │ HTTP/2 over Keep-Alive
                                                 ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                            INGRESS LOAD BALANCER LAYER                           │
│                            (Envoy / Nginx Edge Cluster)                          │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │                            ROUTING ENGINE                                │   │
│  │                                                                          │   │
│  │  • /v1/webhooks/*     ──▶ Webhook Pool (High-Burst Queue, 0 Dropped Req) │   │
│  │  • /v1/driver/*       ──▶ Ultra-Lightweight Low-Latency Driver Pool      │   │
│  │  • /v1/rfq/voice      ──▶ Audio Direct-Upload Gateway (R2 Streaming)     │   │
│  │  • /v1/* (Core API)   ──▶ Stateless Business Logic API Cluster           │   │
│  │  • /ws/*              ──▶ WebSocket Cluster (Consistent Hash by Org ID)  │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────┬───────────────────────────────────────────┘
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             BACKEND SERVICE POOLS                                │
│                                                                                  │
│   ┌───────────────────────────┐                ┌─────────────────────────────┐   │
│   │  Core Java Backend Nodes  │                │  AI Inference Worker Nodes  │   │
│   │  (Spring Boot 3.3+ Loom)  │                │  (Worker Queue Pool)        │   │
│   └───────────────────────────┘                └─────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Webhook Ingress Burst Buffering

Payment webhooks from the Partner MFB and audio message webhooks from Meta WhatsApp can spike up to thousands of requests per second during peak market opening hours:
1. **Immediate Ingress Response**: Webhook handlers acknowledge receipt with HTTP `200 OK` within <100ms after cryptographic HMAC signature validation.
2. **Redis Ingestion Queue**: The payload is pushed onto an in-memory Redis stream for asynchronous processing.
3. **Backpressure Protection**: If backend workers experience load, webhooks are safely buffered in Redis without timing out Meta or the partner bank.

---

## 3. Regional Latency Optimization for Nigerian Networks

1. **Local Edge Presence**: Cloudflare terminates TLS handshakes inside Nigeria (Lagos and Abuja points of presence), avoiding cross-continental round-trips to European or US datacenters for SSL negotiation.
2. **Aggressive Payload Compression**: All JSON responses exceeding 1 KB are compressed using **Brotli** (levels 4–6) with Gzip fallback, reducing transit size by up to 75%.
3. **TCP Tuning for High Packet Loss**: Socket buffers and TCP keep-alive intervals are tuned for wireless cellular connections prone to temporary signal drops in crowded market halls.
