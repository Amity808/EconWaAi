# Week 11 — Route Exceptions, Levies & WhatsApp Mobile Backoffice

> **Goal:** Implement delivery exception handling (agbero/union levies, checkpoints, impassable roads), deliver the WhatsApp 1-tap operational approval desk, and build conversational ops commands.

---

## Monday — Delivery Exception Domain & Reason Codes

- [ ] Implement `DeliveryException` entity and repository in `econwa-logistics`.
- [ ] Enforce Section 20.4 Exception Reasons:
  - `levy_or_toll_demand` (agbero / local government / market union fees)
  - `checkpoint_delay` (law enforcement / revenue stop)
  - `flooding_or_impassable_road` (heavy rain / seasonal disruption)
  - `vehicle_access_restriction` (time-of-day truck restriction)
  - `market_congestion` (loading dock gridlock)
  - `security_concern_route_changed`
  - `location_not_found_after_call`
- [ ] Implement `POST /v1/driver/jobs/{id}/exceptions`:
  - Driver logs exception with reason code, description, and optional photo.
  - Optional `extra_cost_kobo` claim for official tolls or unavoidable union dues.

**Deliverable:** Field drivers can log real-world road disruptions directly from their lightweight PWA.

---

## Tuesday — Real-Time WhatsApp Alerts to Operations Desk

- [ ] Implement automated exception dispatch to EconWaAi Ops WhatsApp group:
  > *"🚨 Delivery Exception on ORD-202610-8472:*  
  > *Driver: Musa (Plate: EKY-481-XA)*  
  > *Location: Sagamu Interchange*  
  > *Reason: checkpoint_delay / levy_or_toll_demand*  
  > *Claim: ₦5,000 for unexpected union clearance.*  
  > *Notes: 'Union officials blocked corridor.'*"
- [ ] Attach interactive WhatsApp action buttons:
  `[ ✅ Approve ₦5,000 ]`, `[ ❌ Reject Claim ]`, `[ 📞 Call Driver ]`.
- [ ] Implement safety-first rule per Section 20.4: Driver safety strictly takes precedence over cargo.

**Deliverable:** Operational disruptions surfaced to staff phones within seconds with interactive action buttons.

---

## Wednesday — 1-Tap Ops Approvals & Financial Adjustments

- [ ] Implement WhatsApp button webhook handler for Ops actions:
  - Tapping `[ Approve ₦5,000 ]`:
    - Validates user role is `ops_agent` or `platform_admin`.
    - Updates `is_approved_by_ops = true`.
    - Adds approved amount to transporter payout ledger.
    - Notifies driver instantly on their PWA: *"✅ Claim for ₦5,000 approved. Proceed with delivery."*
- [ ] If claim is rejected:
  - Prompts ops agent for rejection reason sent to transporter.
- [ ] Implement audit trail recording who approved the extra fee and timestamp.

**Deliverable:** Operational fee approvals resolved in under 60 seconds without logging into a web backoffice.

---

## Thursday — WhatsApp Conversational Command Bot for Staff

- [ ] Implement conversational command parser for authenticated ops phone numbers:
  - `"status ORD-8472"`:
    - Returns real-time order state, payment status, driver name, vehicle plate, and latest GPS coordinates.
  - `"summary today"`:
    - Returns today's active orders, GMV, total virtual account inflows, settled payouts, and open exceptions.
  - `"hold payout org_992"`:
    - Immediately activates security hold on organization's payout account.
- [ ] Implement rate limiting and security checks on ops commands.

**Deliverable:** Operations staff can monitor platform health and execute administrative actions via chat.

---

## Friday — Dispute Mediation Desk & Partial Escrow Releases

- [ ] Implement recipient discrepancy reporting:
  - If delivered goods are damaged or missing, recipient flags discrepancy on WhatsApp within 4 hours of handover.
  - Mandatory upload of damage photos.
- [ ] Implement partial escrow settlement engine:
  - Automatically freezes disputed amount (e.g., value of 2 damaged bags) while releasing undisputed funds to supplier.
- [ ] Build Ops Dispute Mediation view in Next.js portal for resolving claims and processing refunds.

**Deliverable:** Robust dispute mediation with automated partial escrow holds and rapid resolution SLAs.

---

## Weekend / Catch-Up
- Simulate edge cases: simultaneous exception and dispute, driver loses connectivity after logging exception.
- Review ProcureAI Trade Score algorithm parameters for Week 12.
