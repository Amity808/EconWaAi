# EconWaAi (ProcureAI) — Product Requirements Document (PRD)

> **Document Version**: 1.0.0  
> **Date**: October 2026  
> **Target Market**: Nigeria & West Africa B2B Wholesale Commerce  
> **Status**: Approved for Architectural Specification  
> **Related Document**: [Section 20: Nigeria Operating Realities](./Product_re.md)

---

## 1. Executive Summary & Vision

### 1.1 Problem Statement
B2B wholesale commerce across Nigeria accounts for tens of billions of dollars annually, yet remains fragmented, inefficient, and fraught with operational friction:
- **Informal Communication**: Wholesale purchasing happens over phone calls and WhatsApp voice notes in local vernacular (Nigerian Pidgin, Yoruba, Hausa, Igbo) and informal trade units (*mudu*, *derica*, *paint rubber*, *crate*), making procurement tedious and error-prone.
- **Payment & Credit Bottlenecks**: Bank transfer is ubiquitous, but plagued by "fake alert" fraud, slow manual reconciliation, and lack of working capital. Suppliers offer trade credit only to established contacts, while reliable buyers cannot access institutional liquidity to restock.
- **Logistics Friction**: Deliveries rely on informal drivers who navigate via descriptive landmarks, market lines, and plazas rather than street addresses. Route levies, police checkpoints, flooding, and vehicle access curbs frequently cause delays and unexpected cost disputes.
- **Platform Disintermediation**: Buyers and suppliers routinely take repeat orders offline after first contact unless the platform delivers persistent financial, logistics, and dispute protections.

### 1.2 System Vision
**EconWaAi (ProcureAI)** is an intelligent, AI-powered B2B procurement and logistics orchestration platform purpose-built for the Nigerian wholesale ecosystem. It connects **Wholesale Buyers**, **Suppliers & Distributors**, **Transporters & Drivers**, and **CBN-Licensed Microfinance Banks (MFBs)** into a cohesive, trust-minimized trade network.

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             ECONWAAI (PROCUREAI)                                │
│                                                                                  │
│   ┌────────────────────┐   AI Voice / Chat RFQ    ┌──────────────────────────┐   │
│   │   Wholesale Buyer  │ ───────────────────────▶ │   Supplier / Distributor │   │
│   │   (WhatsApp / Web) │ ◀─────────────────────── │      (Portal / WhatsApp) │   │
│   └─────────┬──────────┘    Structured Quotes     └─────────────┬────────────┘   │
│             │                                                   │                │
│             │ Virtual Account / Credit                          │ Payout / PO    │
│             ▼                                                   ▼                │
│   ┌────────────────────┐                          ┌──────────────────────────┐   │
│   │ Partner MFB Engine │ ── Settlement / Escrow ──│   Transporter / Driver   │   │
│   │ (Virtual Accounts) │                          │  (<200KB Web / Offline)  │   │
│   └────────────────────┘                          └──────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. User Personas & Ecosystem Actors

| Persona | Primary Needs | Key Touchpoints | Constraints |
|:---|:---|:---|:---|
| **Wholesale Buyer** | Fast replenishment via voice notes; accurate quotes; supplier credit / BNPL; verified deliveries. | WhatsApp Bot, Lightweight Mobile Web Dashboard. | Fluctuating cash flow; low-spec Android phones; expensive cellular data. |
| **Wholesale Supplier** | Prompt, verified payment alerts (zero fake transfers); steady B2B demand; guaranteed dispatch; inventory turnover. | Web Portal, WhatsApp Notifications, SMS Alerts. | High price volatility (FX/fuel); aversion to non-guaranteed credit terms. |
| **Transporter & Fleet Owner** | Steady load bookings; clear route expectations; transparent compensation for checkpoints/levies. | Web Portal, WhatsApp Dispatch. | Fleet management overhead; cash-flow sensitivity to fuel spikes. |
| **Field Driver** | Precise landmark directions; phone contact with recipient; low data consumption; instant delivery confirmation. | Ultra-lightweight Web PWA (<200KB), WhatsApp interactive messages, SMS fallback. | Low-end smartphone; intermittent network in transit; no app store downloads. |
| **Partner Microfinance Bank** | Compliant KYC verification (NIN/BVN); audited fund flow; low default rates; automated daily reconciliation. | Core Banking API, Webhook Integration, Secure Admin Dashboard. | CBN prudential guidelines; NDPA 2023 compliance; FCCPC digital lending rules. |
| **Operations & Dispute Agent** | Exception triaging (levies, route blocks, quality rejections); fraud mitigation; driver safety monitoring. | Internal Ops Admin Backoffice. | High triage volume during peak market hours (6 AM - 2 PM). |

### 2.1 WhatsApp-First Self-Service Onboarding & Profile Setup
Any participant can complete full registration, profile setup, and operational onboarding directly within WhatsApp without downloading an app or visiting a website:

#### A. Universal Entry & Role Selection
- When a user sends a first message (e.g., *"Hello"*, *"I want to buy"*, *"I sell rice"*), the bot presents an interactive WhatsApp menu to select their role:
  1. `🛒 Wholesale Buyer`
  2. `📦 Wholesale Seller / Supplier`
  3. `🚚 Transporter / Fleet Owner`

#### B. Conversational Business Profile & Location Setup
- **Business Details**: Bot captures Legal / Trade Name (e.g., *"Chinedu & Sons Wholesale"*).
- **Physical Landmark Location**:
  - Trader enters Market Name (e.g., *"Alaba International Market"* or *"Bodija"*).
  - Line / Section and Shop Number (e.g., *"Fancy Line, Block C, Shop 44"*).
  - Landmark text (e.g., *"Behind First Bank ATM, green gate"*).
  - **Map Pin Drop**: The bot prompts the user to use WhatsApp's native **"Share Location"** pin, which the system captures and stores as latitude/longitude in `organization_locations`.
- **On-site Contact**: Full name and phone number of the stall manager or warehouse receiver.
- **Secure KYC Handoff**: A secure, tokenized webview link is sent on WhatsApp for NIN/CAC/selfie verification per Section 8 (no raw identity numbers collected in chat).

#### C. Seller / Supplier Self-Service Catalogue Registration on WhatsApp
- **Voice or Text SKU Setup**: Suppliers can register their catalogue conversationally:
  - *"I sell Mama Gold 50kg bag at 85,000 naira, minimum order 10 bags."*
  - The AI parses product, trade unit, MOQ, and indicative price into their registered catalogue.
- **Photo / PDF Price List Ingestion**: Suppliers can take a photo of their physical warehouse price board or upload a PDF/Excel invoice. The AI OCR extracts the items and populates `supplier_catalog_items` for confirmation.
- **Instant Stock / Price Management**:
  - *"Update Dangote Sugar to 88,000"* or *"Mark Golden Penny Flour out of stock"*.
  - Immediate database synchronization.

#### D. Buyer Self-Service Setup on WhatsApp
- Default delivery destination set via WhatsApp location pin.
- Instant access to voice/text RFQ creation.
- Automated generation of dedicated partner MFB virtual account upon completing Tier 1 KYC.
- On-demand balance and Trade Score checks (*"What is my credit limit?"*).

#### E. Transporter Self-Service Setup on WhatsApp
- Register vehicle classes (vans, 5-tonne, 10-tonne trucks, open trailers).
- Operating trade corridors (e.g., Lagos–Ibadan, Alaba–Trade Fair, Mile 12–Oyingbo).
- Sub-dispatch driver phone numbers for automated job handoff.
- Bank payout account registration with automatic name-match verification.

---

## 3. Platform Operating Models

1. **Marketplace Coordination Model**: EconWaAi acts as a technology orchestrator and transaction facilitator. It matches RFQs to vetted suppliers, coordinates logistics, and records trade data.
2. **Funds Flow (Non-Custodial)**: EconWaAi **never** holds customer deposits or operates as an unlicensed wallet. All funds flow through dedicated virtual accounts issued by a CBN-licensed partner Microfinance Bank (MFB) or licensed Payment Service Provider (PSP).
3. **Credit Provision**:
   - *Supplier Credit*: Recorded and tracked on-platform at the supplier's own risk, with automated reminders and Trade Score impacts.
   - *ProcureAI Credit (BNPL Restocking)*: Funded and underwritten strictly by the partner MFB using EconWaAi's proprietary Trade Score. Disbursements pay suppliers directly—never disbursed as cash to buyers.
4. **Offline & Low-Bandwidth Resilience**: All driver and field interfaces are web-based, zero-install, and engineered to function under 2G/3G connectivity with client-side offline queues.

---

## 4. Registered Supplier Catalogues, Local Synonyms & Units

### 4.1 Registered Supplier Catalogues
All trade and quotation matching on EconWaAi is anchored directly to the **product catalogues registered by verified suppliers on the platform**:
- **Supplier Onboarding & Catalogue Registration**: Upon KYC verification, suppliers register their product inventory either via the Supplier Web Portal, bulk Excel/CSV template upload, or an assisted WhatsApp catalogue upload flow.
- **Catalogue Schema per Supplier**:
  - Canonical product mapping (e.g., mapped to canonical *Parboiled Rice*).
  - Supplier-specific brand name and grade (e.g., *"Mama Gold Premium Long Grain"*, *"Royal Stallion Caprice"*).
  - Supported trade units and pack sizes (*50kg bag*, *25kg bag*, *carton of 24*, *crate*).
  - Minimum Order Quantity (MOQ).
  - Indicative guide price in kobo (base price used for estimates before binding quotations with `valid_until`).
  - Stock availability status (`in_stock`, `low_stock`, `out_of_stock`, `lead_time_days`).
  - Pickup warehouse/stall location from the supplier's verified `organization_locations`.
- **Catalogue Maintenance & Price Sync**: Suppliers update stock levels and prices directly via the web portal or by messaging the EconWaAi Supplier WhatsApp Bot (e.g., sending *"Mama Gold 50kg now out of stock"* or *"Update Dangote Sugar to 85,000"*).

### 4.2 AI RFQ Matching Against Registered Catalogues
When a wholesale buyer submits a voice note or text RFQ, the AI engine extracts commodities and trade units and matches them against the **registered catalogues of suppliers verified in that delivery zone**:
1. **Catalog Lookup**: The AI scans the indexed registered supplier catalogues within the pickup radius of the requested delivery destination.
2. **Targeted Solicitation**: RFQs are routed exclusively to suppliers whose registered catalogue contains the requested items, stock availability, and meeting the requested volume.
3. **Price Benchmark & Auto-Quote**: If enabled by the supplier, the platform can draft an instant quote based on their registered catalogue indicative price, which the supplier simply approves with a single tap.

### 4.3 Local Unit Conversions
Nigerian trade uses non-metric and vernacular measures that vary by commodity, state, and market line. The catalog supports dynamic per-commodity unit conversions, which can also be customized per supplier catalogue:

| Commodity | Local Trade Unit | Standard Equivalent | Conversion Logic |
|:---|:---|:---|:---|
| Rice / Grains | 50kg Bag / 25kg Bag | 50.0 kg / 25.0 kg | Fixed weight per standardized sack |
| Garri / Beans / Grains | *Mudu* (Paint rubber) | ~1.2 kg to 1.5 kg (grains) / 4.0 kg | Varies by grain density and market (Mile 12 vs Bodija) |
| Tomatoes / Peppers | *Crate* / *Basket* | ~25 kg to 40 kg | Category-specific container density |
| FMCG / Beverages | *Carton* / *Pack* | Multi-pack units (e.g., 24 x 500ml) | Configurable pack-count breakdown |
| Palm Oil / Veg Oil | *Jerrycan* (25L) / *Drum* | 25 Litres / 200 Litres | Liquid volumetric conversion |

### 4.4 Product Synonyms & Dialect Mapping
The AI entity resolver maintains a curated synonyms table linked to canonical products and registered supplier catalogue items:
- *Garri Ijebu*, *Garri White*, *Yellow Garri* (differentiated by grade and origin).
- Brand-to-commodity synonym mapping (e.g., "Dangote Sugar 50kg", "Golden Penny Flour", "Indomie Hungry Man").
- Automatic fuzzy mapping of phonetic variations from voice transcriptions to registered supplier SKUs.

---

## 5. Order Lifecycle & RFQ Engine

```
[Buyer Voice / Text RFQ]
           │
           ▼
[AI Structured Parser] ──(Low Confidence)──▶ [Clarifying Questions / Ops Review]
           │ (Confidence ≥ 0.85)
           ▼
[RFQ Broadcast to Suppliers]
           │
           ▼
[Supplier Submits Quotation (with valid_until)]
           │
           ▼
[Buyer Review & Quote Acceptance]
           │
           ▼
[Supplier Reconfirmation (Step 9) — Price Lock / Approval]
           │
           ▼
[Payment / Virtual Account / Credit Selection]
           │
           ▼
[MFB Webhook Payment Confirmation] ──(Screenshot/SMS Prohibited)──▶ [Order Paid]
           │
           ▼
[Logistics Job Dispatched to Transporter]
           │
           ▼
[Driver Pickup & Milestone Tracking]
           │
           ▼
[Handover Inspection & OTP Delivery Confirmation]
           │
           ▼
[MFB Settlement to Supplier & Transporter]
```

### 5.1 Step-by-Step Lifecycle
1. **RFQ Ingestion**: Buyer initiates an order via voice note or text on WhatsApp or web.
2. **AI Extraction**: AI converts informal speech into structured line items with standardized quantities and locations.
3. **Buyer Confirmation**: Platform responds with a structured summary in plain English (and local language) for buyer sign-off.
4. **Quotation Solicitation**: Targeted RFQ sent to qualified suppliers in the designated zone.
5. **Supplier Quoting**: Suppliers submit quote prices with explicit `valid_until` expiration timestamps.
6. **Buyer Selection**: Buyer selects preferred quote.
7. **Supplier Reconfirmation (Step 9)**: Supplier reconfirms inventory availability and price. If price changes due to market shocks, the new price **must** be explicitly approved by the buyer before proceeding.
8. **Checkout & Payment**: Buyer pays via dedicated Virtual Account transfer, Supplier Credit, or ProcureAI Credit.
9. **Fulfillment & Dispatch**: Once payment is verified by MFB webhook, delivery job is dispatched to vetted transporters.
10. **Delivery & Handover**: Goods arrive; recipient inspects; driver enters recipient OTP; delivery confirmed.
11. **Automated Settlement**: Bank releases funds to supplier and transporter.

---

## 6. Pricing, Volatility & Quotation Engine

### 6.1 Price Validity (`valid_until`)
- In volatile markets (FX fluctuations, PMS/fuel price shifts), all quotations and delivery quotes carry an explicit `valid_until` timestamp (default: 24–48 hours for imported goods, 12 hours for fresh food).
- Expired quotes cannot be accepted. The system provides a single-tap "Request Re-Quote" button.

### 6.2 Price History & Anomaly Detection
- System tracks historical unit prices per commodity, supplier, and region.
- AI anomaly checks flag price quotes deviating by >15% from trailing 7-day median, alerting operations to potential errors or gouging.

---

## 7. Logistics, Dispatch & Transporter Network

### 7.1 Dispatch Model
- Delivery jobs are broadcast to a curated, vetted group of transporters operating the required route, rather than unrestricted open broadcasts.
- Job postings display general pickup/delivery zones, weight/volume, and vehicle requirements. Precise landmark directions and contact numbers are only unlocked once the job is accepted and assigned.

### 7.2 Landmark-Based Navigation & Locations
Delivery addresses in Nigerian markets lack standard civic street numbering:
- Location profiles support: `market_name`, `section_or_line`, `shop_or_plaza_number`, `landmark_description`, `map_pin` (lat/long), and `onsite_contact_phone`.
- Recipient phone calls are treated as standard delivery milestones: `driver_called_recipient` event and `location_not_found_after_call` exception reason.

### 7.3 Route Rules & Exception Management
Supported delivery exception codes:
- `levy_or_toll_demand` (agbero / local council / union fees).
- `checkpoint_delay` (law enforcement / revenue stops).
- `flooding_or_impassable_road` (seasonal rainy-season disruptions).
- `vehicle_access_restriction` (e.g., Lagos State daytime truck bans).
- `market_congestion` (loading dock gridlock).
- `security_concern_route_changed`.

---

## 8. Identity, Business Verification & Compliance

### 8.1 Verification Tiers
- **Individuals (Sole Traders)**: National Identity Number (NIN) verified via licensed Identity Verification Provider (IVP). Higher-risk accounts require selfie liveness checks.
- **Corporate Entities**: Corporate Affairs Commission (CAC) status validation, RC/BN number lookup, and representative authority verification.
- **Informal Traders**: Trade association references, market leader endorsement, and guarantor sign-off with conservative initial trading limits.

### 8.2 Privacy & NDPA 2023 Compliance
- Full NIN/BVN numbers are **never stored** in raw form in EconWaAi databases. Only verification status, provider references, and tokenized metadata are retained.
- Explicit, recorded consent (`consents` table) is mandatory before transmitting details to MFB or KYC verification partners.

---

## 9. Trust, Safety & Anti-Fraud

1. **Anti-Fake Alert Defense**: Orders **never** transition to "Paid" via screenshots, forwarded SMS messages, or verbal claims. Only verified cryptographic webhooks from the partner MFB trigger status progression.
2. **Bank Detail Change Freeze**: Any modification of payout bank details immediately freezes payouts for 48 hours and requires two-factor authentication and manual operations clearance.
3. **Load Value Caps**: Transporters are tiered (Tier 1–3). High-value loads (>₦10,000,000) are restricted to Tier 3 transporters with goods-in-transit (GIT) insurance and active telematics tracking.
4. **Anti-Collusion & Circular Trade Detection**: Algorithms scan for circular orders (Buyer A buying from Supplier B with common bank/IP/device footprints) designed to inflate Trade Scores.

---

## 10. AI Processing Engine, Voice & Safeguards

### 10.1 Multi-Lingual Speech-to-Text (STT) & NLU
- Voice notes in English, Nigerian Pidgin, and regional languages (Yoruba, Hausa, Igbo) are processed through fine-tuned Whisper STT models.
- Domain-specific Named Entity Recognition (NER) extracts: `commodity`, `quantity`, `trade_unit`, `delivery_location`, and `urgency`.
- Confidence thresholding: If parsing confidence is < 0.85, the system automatically triggers a targeted clarifying question (e.g., *"Do you mean 50kg bags or 25kg bags of Mama Gold Rice?"*) or routes to a human agent.

### 10.2 Strict AI Safeguard Guardrails
- **Zero Financial Authority**: AI agents are strictly forbidden from approving credit lines, modifying bank account details, altering payout amounts, or overriding dispute settlements.
- **Read-Back Requirement**: All AI-extracted RFQs must be converted to plain text and confirmed by the buyer before reaching any supplier.

---

## 11. Payments, Virtual Accounts & Reconciliation

### 11.1 Dedicated Virtual Accounts
- A unique NUBAN virtual account is dynamically generated per purchase order via the partner MFB API.
- Payment directly references the specific purchase order, achieving 100% automated settlement without manual reconciliation.

### 11.2 Payment Terms
- `pay_before_dispatch` (standard B2B default).
- `pay_on_delivery` (escrow hold released upon recipient OTP verification).
- `part_payment` (deposit paid before loading; balance settled at handover).
- `supplier_credit` (terms agreed directly between buyer and supplier).
- `procureai_credit` (partner MFB BNPL).

---

## 12. ProcureAI Credit & Trade Score Engine

### 12.1 Points Ledger & Trade Score
- Reliable trading earns points: completed orders (+10 pts), on-time credit repayment (+25 pts), verified KYC (+15 pts).
- Negative events reduce score: overdue payment (-30 pts), frivolous dispute (-20 pts).
- Trade Score classifies buyers into Bronze, Silver, and Gold tiers with increasing credit limits and longer repayment terms (7 to 28 days).

### 12.2 Direct Supplier Disbursement
- Credit funds provided by the MFB are wired **directly to the supplier's verified account**. Cash is never disbursed directly to the buyer's personal account, guaranteeing that credit is strictly utilized for inventory restocking.

---

## 13. WhatsApp Conversational Super-App & Operational Backoffice

WhatsApp functions as a primary **conversational interface and mobile backoffice** across all ecosystem participants:

### 13.1 Participant Capabilities on WhatsApp
1. **Wholesale Buyers**:
   - Audio/Voice note RFQ placement in Nigerian Pidgin or English.
   - Interactive quote review with single-tap acceptance.
   - Automatic receipt of dedicated virtual NUBAN payment details.
   - Live delivery milestone tracking and OTP generation for handover.
2. **Wholesale Suppliers (Mobile Backoffice)**:
   - RFQ alerts with interactive single-tap quote buttons (`[ Accept Guide Price ]`, `[ Custom Quote ]`, `[ Decline ]`).
   - Step 9 Order Reconfirmation alerts with stock and price verification buttons.
   - Self-service catalogue registration, voice-based price adjustments, and out-of-stock toggles.
   - Real-time payout notifications upon bank settlement.
3. **Transporters & Fleet Owners**:
   - Route broadcast offers and 1-tap job claiming.
   - Sub-dispatch driver assignment over WhatsApp.
   - Checkpoint levy and delay reporting.
4. **Platform Operations & Admins (Conversational Ops Desk)**:
   - Real-time exception alerts with interactive action buttons (`[ Approve Levy ₦5,000 ]`, `[ Reject ]`, `[ Re-route Driver ]`).
   - Human-in-the-loop AI ambiguity resolution for low-confidence voice transcriptions (<0.85).
   - Conversational query commands (`status ORD-8472`, `summary today`, `hold payout org_123`).

### 13.2 Meta Cloud API Optimization & Cost Safeguards
- **Direct Meta Integration**: Direct connection to Meta Graph API without intermediary BSP markups.
- **24-Hour Free Customer Service Window**: Follow-up queries and order status updates are scheduled inside the 24-hour service window to eliminate outbound template fees.
- **Batched Milestone Templates**: Driver assignment, ETA, and vehicle registration are combined into a single message rather than 3 separate templates.
- **Critical Fallback**: Automated transactional SMS (via Termii) and Voice OTP call when WhatsApp delivery fails or devices are offline.

---

## 14. Mobile & Low-Bandwidth Client Strategy

- **Ultra-lightweight Driver Web PWA**: Total bundle size strictly < 200 KB.
- **Offline Upload Queue**: Driver photo captures (proof of loading, proof of delivery) are compressed on-device (under 150 KB JPEG) and stored in `IndexedDB`. When connectivity drops, submissions are queued and transmitted with original capture timestamps when signal resumes.
- **Zero App Download Requirement**: Drivers and casual buyers access all essential tools via mobile web links sent over WhatsApp or SMS.

---

## 15. Financial Model, Revenue Lines & Billing

1. **Marketplace Commission**: 1.0% to 2.5% fee on completed purchase orders, deducted at supplier payout.
2. **Logistics Coordination Fee**: Small margin (3% to 5%) on delivery quotes for route management, insurance, and tracking.
3. **Fintech Referral & Servicing Fee**: Agreed revenue-share with the partner MFB on interest and loan origination fees generated through ProcureAI Credit.
4. **Transparent Separation**: Financial reports strictly segregate GMV (Gross Merchandise Value of goods), Transporter Fees, and ProcureAI Net Revenue.

---

## 16. Tax, Invoicing & Fiscalization

- Dynamic VAT calculation: VAT rate (currently 7.5%) and exemption status configured at the product category and supplier tier level.
- Multi-Document Architecture:
  1. *Supplier Invoice*: Goods value + applicable VAT issued by Supplier to Buyer.
  2. *Waybill & Logistics Invoice*: Transporter fee + service charge issued to Buyer.
  3. *Platform Service Receipt*: ProcureAI commission and platform charges.
- Ready for integration with Nigeria Revenue Service (NRS) digital fiscalization and e-invoicing APIs.

---

## 17. Dispute Resolution, Handover & Claims

- Delivery handover requires physical inspection of goods before driver departure.
- If goods are damaged or missing, driver and recipient log an on-site discrepancy in the driver web app with mandatory photographs.
- The platform automatically holds the disputed portion of payment in escrow while releasing undisputed balances, targeting dispute resolution within 4 business hours.

---

## 18. Analytics, Operational Dashboards & Audit

- Real-time tracking of:
  - Buyer repeat ordering rate per supplier pair (disintermediation signal detection).
  - Average WhatsApp template cost per completed order.
  - Delivery exception frequency per logistics corridor (Lagos–Ibadan, Onitsha–Enugu, Kano–Kaduna).
  - Daily bank reconciliation status: Virtual account inflows vs MFB ledger payouts.

---

## 19. Non-Functional & Security Requirements

- **Availability**: 99.9% uptime during wholesale trading hours (05:00 to 20:00 West Africa Time).
- **Latency**: STT voice note processing to structured RFQ within < 4.0 seconds; API P99 latency < 250ms.
- **Data Protection**: In accordance with the Nigeria Data Protection Act (NDPA) 2023, personal data is encrypted at rest (AES-256) and in transit (TLS 1.3). PII retention rules enforce automated anonymization upon account termination.

---

## 20. Cross-Reference with Nigeria Operating Realities

This PRD incorporates and formalizes all requirements established in [Product_re.md](./Product_re.md). In the event of any regional implementation conflict, Section 20 of [Product_re.md](./Product_re.md) governs.

---

## 21. 12-Week Delivery Roadmap

See [./roadmap/README.md](./roadmap/README.md) for the phased, day-by-day implementation plan (Weeks 1 to 12 + Week 13 Phase 2).

