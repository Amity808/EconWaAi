# EconWaAi (ProcureAI) — Architecture Documentation

> An AI-powered B2B wholesale procurement and logistics orchestration platform purpose-built for Nigerian and West African trade realities.

---

## Quick Reference

| Dimension | Architectural Decision | Details |
|:---|:---|:---|
| **Primary Domain** | B2B Wholesale Marketplace & Logistics | Buyers, Suppliers, Transporters, Partner MFBs |
| **Backend Core** | Java 21 (Spring Boot 3.3+) — Modular Monolith | Virtual Threads (Project Loom), Spring Security, Spring Data / jOOQ |
| **AI / STT Microservice** | Python 3.11 (FastAPI + Whisper + PyTorch) | Speech-to-text, Pidgin/accent fine-tuned, NER extraction |
| **Frontend Clients** | Next.js 15 (App Router) + Lightweight PWA | Desktop web portal for suppliers/ops; <200KB PWA for drivers |
| **Primary Messaging** | Meta WhatsApp Business Cloud API | Direct Graph API integration, audio webhook ingestion |
| **Database** | PostgreSQL 16 + Redis 7 | Row-Level Security (RLS), JSONB, `pg_trgm`, vector embeddings |
| **Funds Flow & Custody** | Non-Custodial CBN-Licensed Partner MFB | Dedicated virtual NUBAN accounts per PO; automated webhook settlement |
| **Financing / Credit** | ProcureAI Credit (Partner MFB Underwritten) | Trade Score algorithm, direct supplier disbursement, BNPL restocking |
| **Logistics Model** | Landmark & Pin Dispatch + Offline Queue | Non-civic address engine, driver calling milestone, zero-install PWA |
| **Compliance** | NDPA 2023 & CBN Digital Guidelines | No raw BVN/NIN storage, cryptographic consent ledger |

---

## Documentation Index

| # | Document | Description |
|:--|:---------|:------------|
| 01 | [System Overview](./01-system-overview.md) | High-level architecture, C4 context/container diagrams, end-to-end multi-party workflows |
| 02 | [Tech Stack](./02-tech-stack.md) | Core technology selections, benchmark rationale, and crate/library ecosystem |
| 03 | [Multi-Tenancy & Party Model](./03-multi-tenancy.md) | Multi-organization isolation, actor roles, RLS policies, and market branch scopes |
| 04 | [Database Schema](./04-database-schema.md) | Complete PostgreSQL 16 schema including Section 20 operational tables and indexes |
| 05 | [API Design](./05-api-design.md) | RESTful API specifications, webhook contracts, voice note ingestion, and error standards |
| 06 | [Module Breakdown](./06-module-breakdown.md) | Modular monolith crate topology, domain boundaries, and async event buses |
| 07 | [Frontend Architecture](./07-frontend-architecture.md) | Multi-client Next.js apps, ultra-lightweight driver PWA (<200KB), and offline sync |
| 08 | [Infrastructure & Deployment](./08-infrastructure.md) | Cloud topology, Docker/K8s, audio processing pipelines, Redis queues, and CDN edge |
| 09 | [Security & Compliance](./09-security.md) | NDPA 2023 compliance, identity tokenization, webhook HMAC, payout holds, fraud filters |
| 10 | [External Integrations](./10-integrations.md) | Partner MFB Core Banking API, WhatsApp Cloud API, licensed KYC/IVP, SMS fallbacks |
| 11 | [Load Balancing & Ingress](./11-load-balancer.md) | Edge routing, traffic distribution, webhook burst buffering, and regional latency tuning |

---

## Product Requirements Reference

For product specifications and Nigerian operating adaptations, refer to:
- [Product Requirements Document (PRD)](../PRD.md)
- [Section 20: Nigeria Operating Realities](../Product_re.md)

---

## Roadmap

See [../roadmap/README.md](../roadmap/README.md) for the 12-week delivery plan with day-by-day breakdowns.
