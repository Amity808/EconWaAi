# Week 6 — Voice/Text RFQ Engine & Dynamic Quoting

> **Goal:** Connect confirmed buyer RFQs to targeted supplier catalogues, implement quotation submission with explicit `valid_until` windows, and build the automated quote expiration scheduler.

---

## Monday — RFQ Domain Model & Targeted Supplier Matching

- [ ] Implement `Rfq` and `RfqItem` entities and repositories in `econwa-rfq`.
- [ ] Implement `RfqMatcherService`:
  - When an RFQ is confirmed, queries `supplier_catalog_items` for verified suppliers in the destination corridor offering the requested commodities and volume.
  - Excludes suppliers currently marked `out_of_stock`.
- [ ] Implement RFQ broadcast queue in Redis.
- [ ] Write tests verifying that RFQs route strictly to suppliers who have registered matching items in their catalogue.

**Deliverable:** Targeted RFQ matching engine successfully identifies eligible local suppliers.

---

## Tuesday — Supplier Quoting & Price Validity (`valid_until`)

- [ ] Implement `Quotation` and `QuotationItem` entities and repositories in `econwa-rfq`.
- [ ] Implement `POST /v1/rfq/{id}/quotes` for supplier quote submission:
  - Enforces mandatory `valid_until` timestamp per Section 6 (e.g., 24–48 hours for commodities, 12 hours for fresh food).
  - Records unit price in kobo and calculates total amount.
  - Updates `price_history` table with submitted quote prices.
- [ ] Implement AI price anomaly detector:
  - Compares submitted quote against trailing 7-day median for that commodity.
  - Flags quotes deviating by > 15% for ops review before publishing to buyer.

**Deliverable:** Suppliers can submit binding quotes with guaranteed price validity windows.

---

## Wednesday — WhatsApp Interactive Quoting for Suppliers

- [ ] Implement WhatsApp RFQ broadcast notification to matched suppliers:
  > *"📦 New RFQ #RFQ-2041:*  
  > *Buyer wants 20x 50kg bags of Mama Gold Rice to Trade Fair.*  
  > *Your registered guide price: ₦85,000.*  
  > *Quote valid for: 24 hours."*
- [ ] Attach interactive WhatsApp buttons:
  `[ Submit Quote: ₦85,000 ]`, `[ Custom Price ]`, `[ Decline ]`.
- [ ] Handle supplier tap event:
  - Tapping `[ Submit Quote ]` instantly generates a binding `Quotation` record with default validity.
  - Tapping `[ Custom Price ]` prompts for revised amount.

**Deliverable:** Suppliers can review RFQs and submit binding quotes via a single tap on WhatsApp.

---

## Thursday — Buyer Quote Review & Selection

- [ ] Implement quote compilation service:
  - Gathers submitted active quotes for an RFQ.
- [ ] Push compiled quote comparison to buyer over WhatsApp:
  > *"3 suppliers quoted on your RFQ #RFQ-2041:*  
  > *1. Chinedu Stores: ₦85,000/bag (Valid until tomorrow 2 PM)*  
  > *2. Bestway Wholesale: ₦86,200/bag (Valid until tomorrow 6 PM)*  
  > *Reply 1 or 2 to accept."*
- [ ] Implement `POST /v1/quotes/{id}/accept`:
  - Validates that quote has not passed its `valid_until` timestamp.
  - Transitions quote status to `accepted` and moves RFQ to `ordered`.

**Deliverable:** Buyers receive structured quote comparisons on WhatsApp and can accept active quotes with a single reply.

---

## Friday — Automated Expiration Scheduler & One-Tap Re-Quote

- [ ] Implement scheduled background worker in `econwa-rfq` (using Java 21 Virtual Threads):
  - Runs every 5 minutes to identify quotes where `now() > valid_until` and `status = 'submitted'`.
  - Transitions expired quotes to `expired`.
- [ ] Block buyer acceptance of expired quotes per Section 6.
- [ ] Implement One-Tap Re-Quote flow:
  - When a buyer attempts to accept an expired quote, present a `[ Request Re-Quote ]` button.
  - Prompts original supplier to reconfirm or adjust price for another validity window.

**Deliverable:** Zero stale price liabilities; automated quote expiration and re-quote engine operational.

---

## Weekend / Catch-Up
- End-to-end rehearsal of RFQ -> Quoting -> Expiration flow on staging.
- Prepare Step 9 reconfirmation state transitions for Week 7.
