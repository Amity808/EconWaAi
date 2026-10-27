# 01 — System Overview

## 1. What is EconWaAi (ProcureAI)?

**EconWaAi (ProcureAI)** is an AI-powered B2B procurement and logistics orchestration platform specifically engineered for wholesale trade across Nigeria and West Africa. It bridges informal trade mechanics with formal institutional infrastructure:

1. **AI Voice & Multi-Lingual RFQ Processing**: Converts voice notes and texts in Nigerian Pidgin, English, Yoruba, Hausa, and Igbo into structured, normalized purchase orders with standardized trade units.
2. **Dynamic Quoting & Volatility Protection**: Enables multi-supplier bidding with explicit quote validity windows (`valid_until`) to manage rapid FX and fuel price fluctuations.
3. **Non-Custodial Bank Settlement**: Integrates directly with a CBN-licensed partner Microfinance Bank (MFB) to automatically generate dedicated virtual NUBAN accounts per order, eliminating fake payment alert fraud.
4. **ProcureAI Credit & Trade Score Engine**: Leverages proprietary trading, delivery, and repayment data to qualify buyers for institutional BNPL restocking credit, disbursed directly to suppliers.
5. **Landmark-Based Logistics & Driver App**: Dispatches jobs across curated transporter pools using landmark descriptions, market stalls, and map pins with an ultra-lightweight (<200KB) offline-resilient web client.

---

## 2. High-Level C4 Context Diagram

```
                                  ┌─────────────────────────────┐
                                  │   Meta WhatsApp Platform    │
                                  │   (Graph API / Webhooks)    │
                                  └──────────────┬──────────────┘
                                                 │
                                                 ▼
┌──────────────────┐    HTTPS / WS    ┌─────────────────────────────────┐    Banking API   ┌───────────────────────┐
│ Wholesale Buyers │ ◀──────────────▶ │                                 │ ◀──────────────▶ │  Partner MFB Core     │
└──────────────────┘                  │                                 │                  │  (Virtual Accounts)   │
                                      │       ECONWAAI PLATFORM         │                  └───────────────────────┘
┌──────────────────┐    HTTPS / WS    │                                 │
│    Suppliers     │ ◀──────────────▶ │  • Modular Monolith (Java 21)   │    Identity API  ┌───────────────────────┐
│                  │                  │  • Spring Boot 3.3+ (Loom)      │ ◀──────────────▶ │ Licensed IVP / CAC    │
│                  │                  │  • AI STT/NLU Engine (Python)   │                  │ (NIN / BVN Checks)    │
│                  │                  │  • PostgreSQL 16 + Redis 7      │                  │                       │
┌──────────────────┐    <200KB PWA    │                                 │                  └───────────────────────┘
│ Transporters &   │ ◀──────────────▶ │                                 │
│ Drivers          │                  │                                 │    SMS / Voice   ┌───────────────────────┐
└──────────────────┘                  └─────────────────────────────────┘ ◀──────────────▶ │ Termii / Africa's T.  │
                                                                                           │ (Fallback OTPs)       │
                                                                                           └───────────────────────┘
```

---

## 3. Core Component Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                   │
│                                                                             │
│  ┌────────────────────────┐  ┌───────────────────────┐  ┌────────────────┐  │
│  │ WhatsApp Client        │  │ Supplier & Admin Web  │  │ Driver Web PWA │  │
│  │ (Buyers, Suppliers,    │  │ (Next.js 15 SSR)      │  │ (<200KB bundle,│  │
│  │  Drivers via Bot)      │  │                       │  │  Offline sync) │  │
│  └───────────┬────────────┘  └───────────┬───────────┘  └───────┬────────┘  │
└──────────────┼───────────────────────────┼──────────────────────┼───────────┘
               │                           │                      │
               ▼                           ▼                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          EDGE & INGRESS LAYER                               │
│                                                                             │
│  Cloudflare Edge CDN (TLS 1.3 Termination, DDoS Guard, Wildcard Routing)    │
│  Nginx / Envoy Load Balancer (Rate Limiting, Webhook Ingress Buffer)        │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│             APPLICATION LAYER (JAVA 21 / SPRING BOOT 3.3+)                  │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                     SPRING FILTER & INTERCEPTOR CHAIN                 │  │
│  │   Request Tracer → Rate Limiter → Spring Security (JWT) → RLS Scope   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  ┌──────────────────┐ ┌──────────────────┐ ┌─────────────────────────────┐  │
│  │  Auth & Identity │ │  RFQ & Quoting   │ │  Order Lifecycle Service    │  │
│  │  Service (NIN/CAC│ │  Engine          │ │  (Escrow, Milestone State) │  │
│  └──────────────────┘ └──────────────────┘ └─────────────────────────────┘  │
│  ┌──────────────────┐ ┌──────────────────┐ ┌─────────────────────────────┐  │
│  │  Logistics &     │ │  MFB & Payment   │ │  Trade Score & Credit       │  │
│  │  Dispatch Router │ │  Gateway Bridge  │ │  Underwriting Engine        │  │
│  └──────────────────┘ └──────────────────┘ └─────────────────────────────┘  │
│  ┌──────────────────┐ ┌──────────────────┐ ┌─────────────────────────────┐  │
│  │  WhatsApp & Comm │ │  Catalog & Unit  │ │  Finance, Tax & Audit       │  │
│  │  Notification Hub│ │  Conversion Hub  │ │  Reconciliation Engine      │  │
│  └──────────────────┘ └──────────────────┘ └─────────────────────────────┘  │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │           ASYNC WORKER ENGINE (Java 21 Virtual Threads / Tasks)       │  │
│  │  • WhatsApp Webhook Ingestion  • Daily Bank Statement Reconciliation  │  │
│  │  • Quote Expiry Monitor        • Driver Geolocation Event Processor   │  │
│  │  • Audio Pre-processing        • Credit Repayment Reminders           │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬───────────────────────────────┬─────────────────────┘
                        │                               │
                        ▼                               ▼
┌──────────────────────────────────────┐    ┌─────────────────────────────────┐
│     AI STT/NLU INFERENCE ENGINE      │    │         DATA & PERSISTENCE      │
│     (Python 3.11 / FastAPI / Torch)  │    │                                 │
│                                      │    │  ┌───────────────────────────┐  │
│  • Whisper (Fine-tuned for Pidgin)   │    │  │ PostgreSQL 16 (Primary)   │  │
│  • Audio Chunking & Noise Filtering  │    │  │ • RLS Multi-Tenancy       │  │
│  • Named Entity Recognition (NER)    │    │  │ • JSONB + pg_trgm Search  │  │
│  • Unit & Commodity Resolver         │    │  └───────────────────────────┘  │
│  • Intent & Confidence Scoring       │    │  ┌───────────────────────────┐  │
│                                      │    │  │ Redis 7 (Cache & Queues)  │  │
│                                      │    │  │ • Ephemeral Sessions      │  │
│                                      │    │  │ • Audio Job Queue         │  │
│                                      │    │  │ • Webhook Deduplication   │  │
│                                      │    │  └───────────────────────────┘  │
└──────────────────────────────────────┘    └─────────────────────────────────┘
```

---

## 4. End-to-End Trade Lifecycle Flow

```
Buyer             WhatsApp / AI             Core Backend          Supplier           Partner MFB        Transporter / Driver
  │                     │                        │                   │                    │                     │
  │── Voice RFQ ───────▶│                        │                   │                    │                     │
  │   (Pidgin/Eng)      │── Transcribe & NER ───▶│                   │                    │                     │
  │                     │   (Extract Items/Units)│                   │                    │                     │
  │◀── Confirm RFQ ─────│                        │                   │                    │                     │
  │    Structured Text  │                        │                   │                    │                     │
  │── Approved ────────▶│                        │── RFQ Broadcast ─▶│                    │                     │
  │                     │                        │◀── Quote + Window │                    │                     │
  │◀── View Quotes ─────│◀───────────────────────│   (valid_until)   │                    │                     │
  │── Accept Quote ────▶│                        │                   │                    │                     │
  │                     │                        │── Step 9 Reconfirm▶│                    │                     │
  │                     │                        │◀── Confirmed/Price │                    │                     │
  │                     │                        │                   │                    │                     │
  │                     │                        │── Gen Virtual Acc─│───────────────────▶│                     │
  │◀── Pay Details ─────│◀───────────────────────│◀── Account NUBAN ─│────────────────────│                     │
  │    (NUBAN / Credit) │                        │                   │                    │                     │
  │                     │                        │                   │                    │                     │
  │── Bank Transfer ─────────────────────────────────────────────────────────────────────▶│                     │
  │                     │                        │◀── Webhook Paid ──│────────────────────│                     │
  │                     │                        │    (Confirmed)    │                    │                     │
  │◀── Payment Alert ───│                        │── Payment OK Alert▶│                    │                     │
  │                     │                        │                                                              │
  │                     │                        │── Dispatch Job (Landmark + Pin) ────────────────────────────▶│
  │                     │                        │                                                              │── Accept Job
  │                     │                        │◀── Driver Milestone (Picked Up) ─────────────────────────────│
  │◀── Driver En Route ─│◀───────────────────────│                                                              │
  │                     │                        │                                                              │── Driver Calls
  │                     │                        │◀── Event: driver_called_recipient ───────────────────────────│   Recipient
  │                     │                        │                                                              │
  │                     │                        │◀── Submit OTP & Delivery Evidence ───────────────────────────│
  │                     │                        │    (Compressed Photo, Lat/Long)                              │
  │◀── Receipt & Rate ──│◀───────────────────────│                                                              │
  │                     │                        │── Settle Funds ───────────────────────▶│                     │
  │                     │                        │   (Supplier & Transporter Payouts)     │                     │
```

---

## 5. Architectural Principles

1. **Deterministic Payments Only**: No order status may ever advance to "Paid" based on human claim, screenshot, or raw SMS. Status updates require verified webhooks or cryptographic signatures from licensed financial partners.
2. **Bandwidth-First Field Architecture**: Field interfaces for drivers must not exceed 200 KB in total asset bundle size. Offline queues must capture device timestamps and reconcile idempotently.
3. **Strict AI Safeguards**: The AI inference pipeline acts strictly as an information transformer and accelerator. It is architecturally barred from financial authority (approving loans, modifying bank accounts, altering fees).
4. **Resilience to Physical Trade Disruptions**: Route levies, security check delays, and fuel price volatility are first-class architectural domain concepts, rather than system anomalies.
