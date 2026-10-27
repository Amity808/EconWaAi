# Week 7 — Order Lifecycle & Step 9 Reconfirmation

> **Goal:** Implement the order management engine, build the Step 9 supplier price lock / reconfirmation workflow (both web modal and WhatsApp bot), and enforce buyer approval on market price adjustments.

---

## Monday — Order Domain Model & Sealed State Machine

- [ ] Implement `Order` entity and repository in `econwa-orders`.
- [ ] Implement sealed state machine in Java:
  ```java
  public sealed interface OrderState 
      permits Created, PendingSupplierReconfirmation, AwaitingBuyerPriceApproval, 
              AwaitingPayment, PaidEscrow, Dispatched, InTransit, DeliveredPendingConfirmation, 
              Completed, Disputed, Cancelled {}
  ```
- [ ] Implement order number generator (`ORD-YYYYMM-XXXX`) using unique Redis sequence.
- [ ] Calculate `goods_subtotal_kobo`, estimated `logistics_fee_kobo`, and `platform_fee_kobo`.
- [ ] Write unit tests verifying that illegal state transitions throw `IllegalStateException`.

**Deliverable:** Order domain model and strictly enforced lifecycle state machine live.

---

## Tuesday — Step 9 Supplier Order Reconfirmation Engine

- [ ] Implement Section 5 Step 9 Reconfirmation service:
  - When a buyer accepts a quotation, the order transitions to `PendingSupplierReconfirmation`.
  - Blocks payment details from being issued until supplier reconfirms physical stock in warehouse.
- [ ] Implement `POST /v1/orders/{id}/step-9-reconfirm`:
  - Action `reconfirm_current`: Supplier locks existing agreed price; order advances to `AwaitingPayment`.
  - Action `reconfirm_with_price_adjustment`: Supplier updates price due to warehouse gate fee or fuel spikes; order advances to `AwaitingBuyerPriceApproval`.
- [ ] Enforce rule: Price changes **cannot** be applied silently.

**Deliverable:** Step 9 backend service operational with strict buyer price protection safeguards.

---

## Wednesday — Step 9 WhatsApp Conversational Flow

- [ ] Implement instant WhatsApp alert to supplier upon quote acceptance:
  > *"🎉 Buyer accepted your quote for ORD-202610-8472 (20x 50kg Mama Gold Rice).*  
  > *Please verify physical warehouse stock to confirm order:"*  
  > `[ ✅ Confirm & Lock Price ]` `[ ⚠️ Adjust Price ]`
- [ ] If supplier taps `[ Adjust Price ]`:
  - Bot asks: *"Enter new total amount in Naira:"*
  - Captures reason text (e.g., *"Market gate fee increase in Mile 12"*).
- [ ] Push price adjustment notification to buyer:
  > *"⚠️ Price Update on ORD-202610-8472:*  
  > *Supplier updated price from ₦1,700,000 to ₦1,720,000 due to gate fee increase.*  
  > *Do you accept the new price?"*  
  > `[ ✅ Accept New Price ]` `[ ❌ Cancel Order ]`
- [ ] Handle buyer approval or cancellation seamlessly over WhatsApp.

**Deliverable:** Complete Step 9 order reconfirmation workflow executable via WhatsApp interactive buttons.

---

## Thursday — Supplier Web Portal Step 9 Modal (Next.js 15)

- [ ] Implement live WebSocket listener in Next.js Supplier Portal for incoming quote acceptances.
- [ ] Build high-priority Step 9 Reconfirmation Modal:
  - Displays line items, quantity, agreed price, and countdown timer (15 minutes).
  - One-click `Confirm Stock` button.
  - Price adjustment input with mandatory reason dropdown.
- [ ] Update order table status in real time via TanStack Query invalidation.

**Deliverable:** Desktop/tablet web interface for suppliers to handle Step 9 reconfirmations in distributor offices.

---

## Friday — Invoice Generation & Payment Terms Association

- [ ] Implement `PaymentTerms` selection:
  - `pay_before_dispatch`
  - `pay_on_delivery`
  - `part_payment` (requires deposit % definition)
  - `supplier_credit` (supplier terms recorded at own risk)
  - `procureai_credit` (MFB BNPL restocking)
- [ ] Implement multi-document PDF generation in `econwa-finance`:
  - *Supplier Goods Pro-forma Invoice* (issued by Supplier to Buyer).
  - *Estimated Delivery Note* (provisional logistics fee).
- [ ] Store generated PDFs in Cloudflare R2 and attach signed download links to WhatsApp summary.

**Deliverable:** Orders successfully reconfirmed, payment terms attached, and pro-forma invoices generated.

---

## Weekend / Catch-Up
- Test race conditions: quote accepted right at expiry boundary, supplier rejects reconfirmation.
- Prepare Partner MFB virtual account API integration for Week 8.
