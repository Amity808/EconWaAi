# 07 — Frontend Architecture

## 1. Multi-Client Ecosystem

EconWaAi accommodates diverse operational environments across Nigerian wholesale commerce—from air-conditioned distributor offices to chaotic, sun-drenched open-air markets with weak 2G/3G connectivity:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            FRONTEND CLIENT MATRIX                           │
│                                                                             │
│  ┌────────────────────────┐  ┌───────────────────────┐  ┌────────────────┐  │
│  │ WhatsApp Super-App     │  │ Desktop Web Portal    │  │ Driver Web PWA │  │
│  │ & Mobile Backoffice    │  │ (Next.js 15 SSR)      │  │ (<200KB bundle,│  │
│  │ (Buyers, Sellers, Ops) │  │                       │  │  Offline sync) │  │
│  │ • Self-Service Onboard │  │ • High-volume Quoting │  │ • Landmark Nav │  │
│  │ • Voice RFQs / Quoting │  │ • Step 9 Price Lock   │  │ • Recipient Tel│  │
│  │ • WhatsApp Pin Location│  │ • Reconciliation Ops  │  │ • Offline POD  │  │
│  │ • Catalogue Mgmt       │  │ • KYC & Disputes      │  │ • Handover OTP │  │
│  │ • 1-Tap Ops Approvals  │  │ • Visual Corridor Map │  │ • Levy Reports │  │
│  └────────────────────────┘  └───────────────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Driver Ultra-Lightweight Web PWA (<200KB Budget)

### 2.1 Design Constraints
- **Zero App Store Installation**: Drivers receive a secure one-time link over WhatsApp or SMS. Tapping it opens the delivery screen instantly.
- **Strict Bundle Ceiling**: Total downloaded assets (HTML, CSS, JS, icons) **must not exceed 200 KB** to load in under 2 seconds on 2G/3G networks.
- **Sunlight Readability**: High-contrast typography and prominent action buttons for outdoor market use.

### 2.2 Offline-First Sync Architecture
Network connectivity routinely drops in transit between regional wholesale corridors:

```
[Driver Action] (e.g., Take Photo, Record Event)
       │
       ▼
[Client Canvas Compress] ── Compresses POD image to <150KB JPEG
       │
       ▼
[IndexedDB Event Queue] ── Stores event with device_timestamp & GPS lat/long
       │
       ▼
[Service Worker Sync Engine]
       │ (When network returns)
       ▼
[Batch POST /v1/driver/jobs/{id}/events] ── Ingested idempotently by server
```

### 2.3 Field Driver Interface Layout
```
┌────────────────────────────────────────┐
│ EconWa Driver  • Job #ORD-8472   [●]   │
├────────────────────────────────────────┤
│ DESTINATION:                           │
│ Trade Fair Complex, Peace Plaza        │
│ Section: Block C, Shop 44              │
│                                        │
│ LANDMARK DIRECTIONS:                   │
│ "Opposite Central Mosque, enter black   │
│  gate behind First Bank ATM"           │
├────────────────────────────────────────┤
│ RECIPIENT CONTACT:                     │
│ Mr. Segun (Shop Assistant)             │
│ [ 📞 CALL RECIPIENT ]                  │
│ *(Tapping records call event to server)*│
├────────────────────────────────────────┤
│ [ 📍 OPEN PIN IN GOOGLE MAPS ]         │
├────────────────────────────────────────┤
│ DELIVERY ACTIONS:                      │
│ [ 📷 CAPTURE PROOF PHOTO (Offline OK) ]│
│                                        │
│ ENTER RECIPIENT 6-DIGIT OTP:           │
│ [ _ _ _ _ _ _ ]                        │
│                                        │
│ [ ⚠️ REPORT ROAD ISSUE / LEVY / DELAY ]│
│ [ CONFIRM & FINALIZE HANDOVER ]        │
└────────────────────────────────────────┘
```

---

## 3. Supplier & Distributor Portal: Next.js 15

### 3.1 Tech Stack & Structure
- **Framework**: Next.js 15 (App Router) with React Server Components (RSC) to minimize client JavaScript.
- **Styling**: Tailwind CSS with custom enterprise tokens.
- **State Management**: TanStack Query (React Query) for caching and background refetching of live RFQs.

### 3.2 Key Views & Features
1. **Live RFQ Bidding Grid**:
   - Filter by commodity category and destination corridor.
   - Display AI extraction confidence and local trade unit requirements.
   - Built-in `valid_until` picker (12h, 24h, 48h, custom) to prevent stale price liability.
2. **Step 9 Reconfirmation Modal**:
   - When a buyer accepts a quote, the supplier gets an instant modal alert to verify physical warehouse stock.
   - One-click confirmation, or price adjustment with required justification.
3. **Registered Supplier Catalogue Manager**:
   - Comprehensive inventory and catalogue management: manage custom branded packaging, trade units (*50kg bag*, *mudu*, *carton*), MOQs, and indicative pricing.
   - Bulk CSV/Excel spreadsheet upload with automatic canonical commodity mapping.
   - Live stock status toggles (`in_stock`, `low_stock`, `out_of_stock`) with pickup warehouse location assignment.
   - Quick sync via the EconWaAi Supplier WhatsApp Bot for instant price/availability adjustments directly from the warehouse floor.

---

## 4. Platform Ops & Dispute Admin Console

Built within the Next.js enterprise app for EconWaAi operations staff:
- **Corridor Monitoring Dashboard**: Heatmap of active logistics exceptions (police checkpoints, union levies, flooding).
- **KYC Triage Desk**: Side-by-side inspection of CAC documents and NIN selfie liveness match results.
- **Daily Bank Reconciliation Desk**: Side-by-side comparison of MFB core statement entries against order records, flagging discrepancies > ₦0.00.
- **Disintermediation Radar**: Tracks buyer-supplier pair reorder frequency. Pairs that suddenly drop to zero after two transactions are highlighted for account management discovery.

---

## 5. WhatsApp Conversational Super-App & Two-Tier Backoffice Architecture

To support traders and operations staff who operate in market stalls without desktop computers, WhatsApp acts as an end-to-end **Conversational Super-App and Mobile Backoffice**:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      TWO-TIER BACKOFFICE ARCHITECTURE                       │
│                                                                             │
│   ┌──────────────────────────────────┐  ┌────────────────────────────────┐  │
│   │   WhatsApp Conversational Ops    │  │    Next.js 15 Web Portal       │  │
│   │   (Fast, Mobile, On-the-Go)      │  │    (Deep Analytics & Bulk)     │  │
│   ├──────────────────────────────────┤  ├────────────────────────────────┤  │
│   │ • Self-service role onboarding   │  │ • High-volume bulk CSV uploads │  │
│   │ • GPS map pin location drops     │  │ • Live corridor heatmap feeds  │  │
│   │ • Voice/Text catalogue setup     │  │ • Line-by-line bank audit desk │  │
│   │ • 1-tap quote & Step 9 approvals │  │ • Multi-year tax reporting     │  │
│   │ • Real-time checkpoint triage    │  │ • Legal dispute arbitration    │  │
│   │ • Human-in-the-loop AI gating    │  │ • System configuration & RBAC  │  │
│   └──────────────────────────────────┘  └────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 5.1 Self-Service Onboarding & Profile Setup on WhatsApp
1. **Interactive Role Selection**: First-time users select `[ Wholesale Buyer ]`, `[ Wholesale Supplier ]`, or `[ Transporter ]` via interactive WhatsApp menu buttons.
2. **Landmark & GPS Pin Setup**: Users enter market name, line/block, and shop number, then drop their native **WhatsApp Location Pin** to record exact latitude/longitude.
3. **Tokenized KYC Handoff**: A secure, single-use webview URL is delivered for camera selfie liveness and NIN/CAC verification (preventing plaintext identity transmission in chat).

### 5.2 Supplier Mobile Backoffice on WhatsApp
- **Voice/Text Catalogue Registration**: Suppliers send voice notes or text listing commodities, brands, trade units (*50kg bag*, *carton*), indicative prices, and MOQs.
- **Price Board Photo Ingestion**: Suppliers upload photos of their warehouse chalkboard or paper invoices; the AI OCR pipeline extracts and populates `supplier_catalog_items`.
- **Instant Stock / Price Sync**: Fast commands (e.g., *"Update Mama Gold to 88,000"*, *"Sugar is out of stock"*) immediately update live inventory.
- **1-Tap Step 9 Reconfirmation**: Order reconfirmation prompts include interactive `[ Confirm Stock & Price ]` and `[ Adjust Price ]` buttons.

### 5.3 Internal Operations Mobile Backoffice on WhatsApp
- **Real-Time Exception Alerts**: Pushes instant notifications for checkpoint delays, flood detours, and union levy claims with 1-tap approval buttons (`[ Approve ₦5,000 ]`, `[ Reject ]`).
- **Human-in-the-Loop AI Disambiguation**: When voice transcription confidence is < 0.85, the audio snippet and ambiguity options are routed to an ops WhatsApp channel for 1-tap human resolution.
- **Conversational Audit Commands**: Authorized phone numbers can query `status <order_id>`, `summary today`, and `hold payout <org_id>`.
