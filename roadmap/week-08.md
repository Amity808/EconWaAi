# Week 8 — Partner MFB Virtual Accounts & Settlement Engine

> **Goal:** Integrate the Partner Microfinance Bank (MFB) Core Banking API to dynamically generate dedicated virtual NUBAN accounts per purchase order and build the verified webhook settlement engine with anti-fake alert fraud defense.

---

## Monday — Partner MFB API Client & Virtual NUBAN Generation

- [ ] Implement `PartnerMfbClient` in `econwa-mfb` using Spring `RestClient` with mTLS and API key authentication.
- [ ] Implement `POST /api/v2/virtual-accounts` integration:
  - Generates a dedicated 10-digit NUBAN mapped to the order ID.
  - Returns `account_number`, `bank_name`, and account name formatted as `EconWa / [Buyer Trade Name]`.
- [ ] Persist virtual account record in `bank_accounts` table.
- [ ] Enforce Section 20.1: Each order gets a unique virtual account so payments reconcile 1:1 automatically.

**Deliverable:** Order transitions from `Step9Reconfirmed` to `AwaitingPayment` with an active dedicated NUBAN.

---

## Tuesday — Verified Webhook Settlement Handler (Anti-Fake Alert)

- [ ] Implement `POST /v1/webhooks/mfb-settlement`:
  - Enforce HMAC-SHA256 signature verification over incoming webhook payload using the bank's secret key.
  - Enforce idempotency: verify `provider_reference` has not been processed previously.
- [ ] Persist payload in `bank_transactions` table.
- [ ] Enforce Section 20.1 Core Rule:
  - **Orders mark as `paid_escrow` ONLY upon verified bank webhook.**
  - Screenshots, forwarded SMS messages, and verbal claims are strictly blocked from mutating payment status.
- [ ] Handle partial payments: update `amount_paid_kobo` and track outstanding balance.

**Deliverable:** Orders automatically transition to `PaidEscrow` within 60 seconds of buyer transfer; zero fake alert vulnerability.

---

## Wednesday — WhatsApp Payment Prompts & Alerts

- [ ] When an order enters `AwaitingPayment`, deliver formatted payment card on WhatsApp:
  > *"💳 Payment Details for ORD-202610-8472:*  
  > *Bank: EconWa Partner MFB*  
  > *Account Number: 9928374610*  
  > *Account Name: EconWa / Chinedu Wholesale*  
  > *Amount Due: ₦1,720,000*  
  > *(Transfer exact amount. Your payment verifies automatically via bank webhook within 60 seconds.)"*
- [ ] When webhook settles, push instant confirmation to both parties:
  - To Buyer: *"✅ Payment of ₦1,720,000 verified. Funds held securely in escrow. Dispatching transporter now."*
  - To Supplier: *"💰 Payment confirmed for ORD-202610-8472. Prepare goods for driver pickup."*

**Deliverable:** Real-time buyer and supplier payment notifications dispatched over WhatsApp.

---

## Thursday — Supplier-Attested POS & Cash Recording

- [ ] Implement Section 20.1 requirement:
  - Wholesale trade occasionally involves direct cash or POS payments to the supplier's personal terminal.
- [ ] Implement `POST /v1/orders/{id}/supplier-attested-payment`:
  - Supplier confirms receipt of cash or external POS slip.
  - Recorded as `payment_method = 'supplier_attested_cash'`.
- [ ] Keep supplier-attested transactions strictly segregated from platform-verified MFB inflows in all financial reports.
- [ ] Require buyer confirmation over WhatsApp before advancing order status.

**Deliverable:** Direct supplier payments tracked without contaminating audited platform banking ledgers.

---

## Friday — Fallback Payment Gateway & Payout Engine

- [ ] Implement fallback payment provider (licensed PSP e.g., Paystack/Monnify) to issue virtual accounts if the partner MFB's API experiences downtime.
- [ ] Implement supplier payout disbursement service:
  - Prepares payout transfers from escrow to supplier verified accounts upon delivery confirmation.
  - Verifies that `payout_hold_until` is not active on the recipient organization.
- [ ] Verify name-match logic before every outbound payout.

**Deliverable:** Resilient dual-route virtual account infrastructure and payout disbursement engine tested.

---

## Weekend / Catch-Up
- Run end-to-end simulated bank transfer testing in MFB sandbox.
- Finalize transporter route definitions and vehicle matching rules for Week 9.
