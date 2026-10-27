# Week 9 — Landmark-Based Logistics & Transporter Dispatch

> **Goal:** Build the logistics dispatch engine, implement landmark-based address resolution, manage curated transporter broadcast pools with masked destinations, and allocate drivers.

---

## Monday — Logistics Domain & Delivery Job Creation

- [ ] Implement `DeliveryJob`, `RouteRule`, and `DeliveryEvent` entities in `econwa-logistics`.
- [ ] Implement automated job creation triggered by `MfbPaymentSettledEvent`:
  - Extracts pickup location from `supplier_catalog_items` warehouse.
  - Extracts delivery location from buyer's registered `organization_locations`.
  - Calculates required vehicle class based on weight and volume (e.g., 20 bags = 1 tonne ➔ 1.5-tonne mini truck).
- [ ] Calculate agreed transporter delivery fee based on corridor base rate + weight multipliers.

**Deliverable:** Confirmed orders automatically spawn a `DeliveryJob` in `broadcast` state.

---

## Tuesday — Curated Transporter Broadcast & Data Masking

- [ ] Implement Section 7.1 Curated Broadcast Pool:
  - Broadcasts jobs exclusively to vetted transporters registered for that route corridor (e.g., Mile 12 to Trade Fair).
  - Strictly avoids open public broadcasts.
- [ ] Enforce Section 7.4 Data Masking RLS policy:
  - Open broadcast jobs display general pickup zone, general delivery market, weight, and fee.
  - **Exact landmark text, shop numbers, and contact names are masked** until a transporter accepts the job.
- [ ] Implement `GET /v1/logistics/jobs/available` with masked fields.

**Deliverable:** Transporters can view available corridor loads without exposing sensitive buyer/supplier coordinates.

---

## Wednesday — Job Claiming & Driver Assignment

- [ ] Implement `POST /v1/logistics/jobs/{id}/claim`:
  - Transporter fleet owner claims available job.
  - Generates 6-digit cryptographic handover OTP hash (`handover_otp_hash`).
- [ ] Implement driver assignment:
  - Transporter selects a registered driver from their organization.
  - Transitions job status to `assigned`.
- [ ] Once assigned, unlock full destination details:
  - Precise landmark description, market line/shop number, map pin, and recipient phone.
- [ ] Deliver driver assignment notification via WhatsApp and SMS with secure one-time field link.

**Deliverable:** Jobs successfully claimed and drivers assigned; full landmark directions unlocked exclusively for assigned driver.

---

## Thursday — Route Rules, Corridors & Night Transit Restrictions

- [ ] Implement `RouteRule` engine:
  - Evaluates time-of-day truck restrictions (e.g., Lagos State daytime heavy vehicle curbs).
  - Automatically widens delivery schedule windows if `rainy_season_active = true` on flood-prone corridors (e.g., Apapa/Oshodi).
- [ ] Implement High-Value Load Safeguards per Section 20.4:
  - Loads > ₦10,000,000 restrict to Tier 3 transporters with goods-in-transit (GIT) insurance.
  - Prohibit unaccompanied night movement unless transporter's security escort protocol is verified.

**Deliverable:** Intelligent route engine enforcing local traffic restrictions and high-value load security ceilings.

---

## Friday — Transporter Portal (Next.js 15) & WhatsApp Job Alerts

- [ ] Build Transporter Fleet Portal in Next.js 15:
  - Live available loads board with corridor filters.
  - Active fleet tracking view showing driver transit statuses.
- [ ] Implement WhatsApp Transporter Dispatch alerts:
  > *"🚚 New Delivery Job #JOB-8472:*  
  > *Route: Mile 12 ➔ Trade Fair Complex*  
  > *Cargo: 1,000 kg (Grains)*  
  > *Fee: ₦35,000.*"  
  > `[ Claim Job ]` `[ Ignore ]`
- [ ] Verify driver assignment sub-dispatch over WhatsApp.

**Deliverable:** Fleet owners can monitor active transit on web and claim jobs via WhatsApp.

---

## Weekend / Catch-Up
- Review pilot driver phone specifications (Android Go edition compatibility).
- Prepare ultra-lightweight PWA bundle configuration for Week 10.
