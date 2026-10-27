# 10 — External Integrations

## 1. Partner Microfinance Bank (MFB) Core Banking Integration

EconWaAi operates a non-custodial model. Funds, virtual NUBAN accounts, and credit underwriting reside with a CBN-licensed partner Microfinance Bank:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PARTNER MFB INTEGRATION                           │
│                                                                             │
│  EconWaAi Backend                          Partner MFB Core Banking Engine  │
│        │                                                 │                  │
│        │── POST /api/v2/accounts/virtual-nuban ─────────▶│ (Generates NUBAN)│
│        │◀── { account_number: "9928374610", bank: "..." }│                  │
│        │                                                 │                  │
│        │◀── POST /webhooks/settlement (HMAC Signed) ─────│ (Buyer pays PO)  │
│        │                                                 │                  │
│        │── POST /api/v2/transfers/disburse ─────────────▶│ (Settles supplier│
│        │◀── { status: "success", tx_ref: "..." }─────────│  & transporter)  │
│        │                                                 │                  │
│        │── GET /api/v2/reconciliation/daily-statement ──▶│ (Midnight audit) │
│        │◀── CSV / MT940 End-of-Day Ledger ───────────────│                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.1 Virtual Account API Contract
- **Endpoint**: `POST https://api.partnerbank.ng/v2/virtual-accounts`
- **Authentication**: Mutual TLS (mTLS) + HMAC API Key
- **Response**: Allocates an instant 10-digit NUBAN mapped to the EconWaAi purchase order.

### 1.2 Automated Daily Statement Reconciliation
Every day at 23:59 WAT, the system fetches the official bank ledger. An automated reconciliation worker verifies that:
1. Every credit in `bank_transactions` matches an active order in `orders`.
2. Every supplier payout equals the order's `goods_subtotal_kobo` minus platform commission.
3. Every transporter payout matches the approved logistics quote.
4. Any mismatch > ₦0.00 raises an automated escalation ticket.

---

## 2. Meta WhatsApp Business Cloud API (Direct)

### 2.1 Direct Meta Graph API Connection
- **Provider**: Direct integration with Meta Graph API (`https://graph.facebook.com/v21.0/{phone_number_id}/messages`). No intermediary BSP (e.g., Twilio) is used, avoiding third-party markups.
- **Audio Ingestion**: WhatsApp sends a webhook event with `type: "audio"` containing a `media_id`. The backend fetches the binary stream directly from Meta and buffers it into Cloudflare R2 for transcription.

### 2.2 Cost Optimization Strategy
- **24-Hour Free Service Window**: Whenever a buyer initiates contact, all replies over the subsequent 24 hours are free of Meta template charges. The conversational bot structures follow-up questions to maximize completion within this window.
- **Batched Milestone Templates**: Instead of sending three individual updates (Transporter assigned, Driver assigned, Estimated arrival), EconWaAi groups them into a single comprehensive notification.

---

## 3. Licensed Identity Verification Providers (IVP)

Integrated with licensed Nigerian verification aggregators (e.g., Prembly / Identitypass, Smile ID):
- **NIN Verification**: Validates National Identity Number with user consent.
- **Selfie Liveness Detection**: Runs 3D facial liveness matching for high-value supplier payouts and Tier 3 transporters.
- **CAC Business Search**: Validates RC/BN registered business names and confirms company active status with the Corporate Affairs Commission.

---

## 4. Fallback Messaging & OTP Providers

WhatsApp delivery can fail due to device network constraints or battery depletion:
- **Primary Fallback (Transactional SMS)**: Integrated with Termii using dedicated transactional DND-whitelisted Sender IDs.
- **Secondary Fallback (Voice OTP)**: If SMS fails within 60 seconds, an automated text-to-speech voice call delivers the 6-digit OTP code directly to the user's mobile number.
