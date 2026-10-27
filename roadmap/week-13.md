# Week 13 — Phase 2: Post-Launch Enhancements & Regional Vernacular

> **Goal:** Expand multi-lingual voice recognition across major regional languages (Yoruba, Hausa, Igbo), integrate Nigeria Revenue Service (NRS) digital e-invoicing fiscalization, and introduce GPS telematics fleet tracking.

---

## Monday — Regional Language Audio Dataset & Acoustic Fine-Tuning

- [ ] Ingest regional wholesale market voice datasets:
  - Yoruba (Bodija Market, Ibadan; Dugbe Market).
  - Hausa (Dawanau Grain Market, Kano; Central Market, Kaduna).
  - Igbo (Main Market, Onitsha; Ariaria International Market, Aba).
- [ ] Fine-tune acoustic models for vernacular trade terms (*"derica"*, *"dawa"*, *"garri ijebu"*).
- [ ] Benchmark speech transcription accuracy across regional dialects.

**Deliverable:** Voice-to-RFQ engine supporting Yoruba, Hausa, and Igbo with > 85% parsing accuracy.

---

## Tuesday — Regional Vernacular Confirmation Messages

- [ ] Implement multi-lingual read-back confirmations on WhatsApp:
  - Deliver order confirmations in the buyer's preferred regional language alongside plain English.
- [ ] Add regional language voice synthesis (Text-to-Speech) for voice note confirmation fallbacks.
- [ ] Test with traders across northern and eastern trade corridors.

**Deliverable:** Native vernacular voice and text confirmations live in WhatsApp bot.

---

## Wednesday — Nigeria Revenue Service (NRS) e-Invoicing Fiscalization

- [ ] Integrate Nigeria Revenue Service (NRS) digital fiscalization APIs per Section 20.10.
- [ ] Implement automated cryptographic invoice signing:
  - Generates verifiable QR codes on Supplier Goods Invoices and Logistics Receipts.
  - Submits required e-invoice payloads to government tax servers without latency impact on checkout.
- [ ] Verify VAT reporting per category (exempt basic foodstuffs vs standard rate goods).

**Deliverable:** Platform invoices certified for national e-invoicing compliance.

---

## Thursday — Transporter Telematics & Live Map Fleet Tracking

- [ ] Integrate GPS telematics provider APIs for commercial haulage trucks (Tramigo / Cartrack).
- [ ] Display real-time truck position on Next.js Transporter Portal and Admin Corridor Monitor.
- [ ] Implement automatic corridor geofencing:
  - Emits automated `arrived_market` events when vehicle enters within 500m radius of destination.

**Deliverable:** Live GPS telematics tracking for high-value freight loads.

---

## Friday — Retrospective, Metric Review & Phase 2 Rollout

- [ ] Review 30-day pilot KPIs:
  - Total Gross Merchandise Value (GMV).
  - Average voice-to-RFQ completion time.
  - WhatsApp template cost per completed order (target: < ₦150 per order).
  - Credit repayment default rate (target: < 2.0%).
  - Driver landmark delivery success rate without operator escalation.
- [ ] Plan Phase 3 expansion into cross-border West African trade corridors (Lagos–Cotonou–Lomé).

**Deliverable:** Phase 2 features deployed; platform positioned for national and regional scale.
