# EconWaAi (ProcureAI) — 12-Week Delivery Roadmap

> A phased, day-by-day implementation plan for building EconWaAi's AI-powered B2B wholesale procurement and logistics platform for Nigeria.

---

## 1. Overview & Phase Alignment

| Week | Phase | Focus |
|:-----|:------|:------|
| **Week 1** | Foundation & External Onboarding | Java 21 / Spring Boot 3 Maven workspace, PostgreSQL 16 schema, Docker, Meta WhatsApp verification kick-off, Partner MFB onboarding |
| **Week 2** | Identity, Consents & Multi-Party Tenancy | Spring Security JWT, Multi-party Organizations (Buyer, Supplier, Transporter), NDPA 2023 consent ledger, PostgreSQL RLS, Tokenized KYC |
| **Week 3** | WhatsApp Gateway & Self-Service Onboarding | Meta Cloud API webhooks, HMAC verification, interactive role selection, landmark location setup, WhatsApp GPS pin drop |
| **Week 4** | AI Voice Note & Speech-to-Text Pipeline | Python Faster-Whisper microservice, Pidgin/accent tuning, WhatsApp .ogg audio ingestion, trade unit NER (*mudu*, *derica*, *50kg bag*) |
| **Week 5** | Registered Supplier Catalogues & Unit Conversions | Supplier catalog registration, custom trade units, bulk CSV/Excel upload, WhatsApp voice/text price updates, stock toggles |
| **Week 6** | Voice/Text RFQ Engine & Dynamic Quoting | Voice RFQ plain-language confirmation, targeted RFQ matching against registered catalogues, quotes with explicit `valid_until` windows |
| **Week 7** | Order Lifecycle & Step 9 Reconfirmation | Order state machine, Step 9 supplier price lock / reconfirmation modal & WhatsApp flow, buyer re-approval of price changes |
| **Week 8** | Partner MFB Virtual Accounts & Settlement | Dedicated virtual NUBAN generation per order, verified MFB webhook settlement (anti-fake alert defense), escrow holds, POS tracking |
| **Week 9** | Landmark Logistics & Transporter Dispatch | Controlled transporter broadcast pool, landmark geocoding engine, vehicle fleet matching, and job assignment unlocking full directions |
| **Week 10** | Ultra-Lightweight Driver PWA (<200KB) | PWA field client (<200KB bundle), client canvas photo compression (<150KB), `IndexedDB` offline queue, recipient phone call milestone, OTP |
| **Week 11** | Route Exceptions, Levies & Mobile Backoffice | Exception triage (`levy_or_toll_demand`, `checkpoint_delay`), 1-tap WhatsApp approval buttons, conversational admin bot, audit logging |
| **Week 12** | ProcureAI Credit, Reconciliation & Launch | Trade Score calculation, Points ledger, MFB direct supplier credit disbursement, daily midnight bank reconciliation cron, pilot rollout |
| **Week 13** | *Phase 2 (Post-Launch)* | Regional language expansion (Yoruba, Hausa, Igbo STT), NRS e-invoicing fiscalization, GPS telematics fleet tracking |

---

## 2. Daily Work Schedule

Each working day represents **6–8 hours of focused engineering**.

| File | Week | Milestone / Deliverable |
|:---|:---|:---|
| [week-01.md](./week-01.md) | Week 1 | Foundation, Maven multi-module setup, DB migrations, Partner MFB & Meta applications |
| [week-02.md](./week-02.md) | Week 2 | Auth, multi-party RBAC, RLS tenant enforcement, NDPA 2023 consents |
| [week-03.md](./week-03.md) | Week 3 | WhatsApp webhook engine, HMAC verification, conversational role onboarding & pin drops |
| [week-04.md](./week-04.md) | Week 4 | Python Faster-Whisper microservice, Pidgin audio pipeline, NER trade unit extractor |
| [week-05.md](./week-05.md) | Week 5 | Registered supplier catalogues, custom trade units, bulk CSV upload, WhatsApp sync |
| [week-06.md](./week-06.md) | Week 6 | Voice RFQs, AI catalogue matching, multi-supplier quotes with `valid_until` expiry |
| [week-07.md](./week-07.md) | Week 7 | Order lifecycle, Step 9 supplier reconfirmation modal & WhatsApp bot, buyer approval |
| [week-08.md](./week-08.md) | Week 8 | Partner MFB virtual accounts, webhook settlement engine, zero-fake-alert escrow |
| [week-09.md](./week-09.md) | Week 9 | Transporter dispatch pool, landmark geocoder, driver route allocation |
| [week-10.md](./week-10.md) | Week 10 | Ultra-lightweight Driver PWA (<200KB), canvas photo compressor, `IndexedDB` offline queue |
| [week-11.md](./week-11.md) | Week 11 | Road exceptions (levies/checkpoints), WhatsApp 1-tap approvals, conversational ops desk |
| [week-12.md](./week-12.md) | Week 12 | ProcureAI Trade Score, BNPL restocking disbursement, daily bank audit, pilot launch |
| [week-13.md](./week-13.md) | Week 13 | *Phase 2* — Regional vernacular STT (Yoruba, Hausa, Igbo), NRS e-invoicing |

---

## 3. Dependency Architecture

```
Week 1 ──▶ Week 2 ──▶ Week 3 ──▶ Week 4 (Core Infrastructure & Voice AI)
                          │
               ┌──────────┴──────────┐
               ▼                     ▼
            Week 5                Week 9  (Catalogues & Logistics Dispatch)
               │                     │
               ▼                     ▼
            Week 6                Week 10 (Dynamic Quotes & Driver PWA)
               │                     │
               ▼                     │
            Week 7 ───────────────┬──┘
               │                  │
               ▼                  ▼
            Week 8             Week 11    (MFB Settlement & Road Exceptions)
               │                  │
               └──────────┬───────┘
                          │
                          ▼
                       Week 12            (ProcureAI Credit, Reconciliation & Launch)
                          │
                          ▼
                       Week 13            (Phase 2 — Post-Launch Enhancements)
```
