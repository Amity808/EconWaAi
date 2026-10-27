# Week 12 — ProcureAI Credit, Daily Bank Reconciliation & Launch

> **Goal:** Deploy the ProcureAI Trade Score and BNPL credit engine with direct supplier disbursement, launch the automated midnight bank statement reconciliation cron, conduct end-to-end corridor dry runs, and deploy the production pilot.

---

## Monday — ProcureAI Trade Score & Points Ledger Engine

- [ ] Implement `TradeScore`, `PointsLedger`, and `CreditLine` entities in `econwa-credit`.
- [ ] Implement mathematical Trade Score calculator (0 to 1,000 points):
  - Completed orders volume and consistency (+10 pts per completed order).
  - Repayment punctuality (+25 pts for on-time repayment).
  - Dispute history (-20 pts per buyer-fault dispute).
  - Deductions for late payments (-30 pts).
- [ ] Configure credit tier mapping per Section 20.1.2:
  - `Starter`: No credit.
  - `Bronze`: 7-day limit (small working capital).
  - `Silver`: 14-day limit.
  - `Gold`: Up to partner lender's maximum limit.
- [ ] Implement explainable score breakdown view visible to buyers on WhatsApp (*"Your Trade Score is 780 (Silver Tier). Restock on credit up to ₦5,000,000"*).

**Deliverable:** Transparent, auditable Trade Score engine live with dynamic tier progression.

---

## Tuesday — ProcureAI Credit (BNPL Restocking) & Direct Supplier Disbursement

- [ ] Implement `POST /v1/credit/drawdown`:
  - Validates buyer has sufficient unused credit limit and no past-due credit balances.
  - Displays plain-language loan terms: principal, interest/fees, total repayable, due date.
  - Enforces authenticated buyer acceptance (OTP or tokenized signature link; plain text is barred).
- [ ] Enforce Section 20.1.2 Responsible Lending Rule:
  - **Credit funds disburse DIRECTLY to the supplier's verified account.**
  - Funds are never paid as cash to the buyer, ensuring credit is used strictly for restocking.
- [ ] Record drawdown in `credit_drawdowns` and freeze additional credit if repayment is overdue.

**Deliverable:** Institutional restocking credit live with direct-to-supplier fund distribution.

---

## Wednesday — Daily Midnight Bank Statement Reconciliation Cron

- [ ] Implement scheduled background worker in `econwa-finance` executing nightly at 23:59 Lagos Time (WAT):
  - Fetches the official settled statement dump (CSV / MT940) from the Partner MFB Core Banking API.
  - Compares raw statement transaction references line-by-line against `bank_transactions`.
  - Verifies:
    1. Every credit matches a verified purchase order.
    2. Every debit matches an approved supplier/transporter payout.
    3. Platform commission deductions match agreed fee percentages.
- [ ] Generate automated Reconciliation Audit Report:
  - Discrepancies > ₦0.00 trigger high-priority alerts to engineering and finance on-call rotations.

**Deliverable:** 100% automated daily bank reconciliation running in production.

---

## Thursday — WhatsApp Messaging Cost Tracking & Margin Audit

- [ ] Implement Section 20.9 Messaging Cost Tracker:
  - Log every outbound WhatsApp template with its category and Meta cost in `message_costs`.
  - Calculate average WhatsApp template expense per completed purchase order.
- [ ] Implement Disintermediation Radar per Section 20.8:
  - Analyzes buyer-supplier pair reorder frequency.
  - Highlights pairs that transact once or twice and then cease activity for discovery review.
- [ ] Verify multi-document tax separation (Supplier Goods Invoice, Logistics Fee, Platform Commission).

**Deliverable:** Unit economics protected; messaging costs and retention health visible in real time.

---

## Friday — End-to-End Rehearsal & Production Pilot Launch

- [ ] Conduct live corridor dry-run on pilot route (e.g., Mile 12 Market ➔ Trade Fair Complex):
  1. Buyer registers and places voice note RFQ on WhatsApp in Nigerian Pidgin.
  2. AI transcribes, extracts items, and reads back plain English confirmation.
  3. Matched suppliers receive alert and submit quotes with `valid_until` windows.
  4. Buyer accepts quote; supplier reconfirms in Step 9.
  5. Dedicated MFB virtual account is generated; simulated transfer settles via webhook.
  6. Job dispatched to transporter; driver navigates via landmark directions on PWA.
  7. Driver calls recipient, arrives at stall, and finalizes delivery with recipient OTP and POD photo.
  8. Funds disburse to supplier and transporter; buyer awards Trade Score points.
- [ ] Execute production deployment to Cloudflare + Cloud cluster.
- [ ] Open pilot onboarding for the initial cohort of 50 verified wholesale traders.

**Deliverable:** EconWaAi (ProcureAI) live in production for pilot commercial trade.

---

## Weekend / Catch-Up
- Monitor live transaction logs and webhook queues.
- Review initial pilot trader feedback and operational telemetry.
