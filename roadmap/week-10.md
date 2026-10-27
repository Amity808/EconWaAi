# Week 10 — Ultra-Lightweight Driver PWA (<200KB) & Offline Queue

> **Goal:** Build the zero-install, ultra-lightweight field driver PWA (<200KB bundle), implement on-device image compression, browser `IndexedDB` offline event queues, recipient call milestones, and OTP delivery completion.

---

## Monday — Zero-Install PWA Setup & Bundle Budget (<200KB)

- [ ] Initialize lightweight driver frontend client (Preact / Vanilla JS + Tailwind JIT).
- [ ] Enforce strict asset size ceiling:
  - Total downloaded payload (HTML, JS, CSS, SVG icons) **must not exceed 200 KB**.
  - Zero heavy third-party dependencies; pure browser-native APIs.
- [ ] Implement secure Magic Link authentication:
  - Driver opens SMS/WhatsApp link (`https://driver.econwa.ai/j/018f4b22-...?token=...`).
  - Single-use cryptographically signed token authenticates session into `localStorage`.
- [ ] Configure Web App Manifest and Service Worker for basic caching.

**Deliverable:** Driver field client loads in < 1.5 seconds on simulated 2G/3G throttled connection.

---

## Tuesday — Landmark Navigation, Google Maps & Recipient Call Milestone

- [ ] Implement high-contrast driver job view:
  - Displays market destination, section/line, shop/stall number, and landmark text.
  - "Open in Maps" button passing coordinates to Google Maps / device default navigation.
- [ ] Implement Section 20.2 Recipient Call Milestone:
  - Prominent `[ 📞 Call Recipient ]` button (`tel:+234...`).
  - Tapping records a `driver_called_recipient` event with client timestamp.
- [ ] Implement `location_not_found_after_call` exception flag if recipient is unreachable.

**Deliverable:** Drivers can navigate via local landmarks and log recipient phone contact milestones.

---

## Wednesday — Client-Side Canvas Image Compression

- [ ] Implement client-side photo capture using HTML5 `<input type="file" accept="image/*" capture="environment">`.
- [ ] Implement on-device canvas resizing and compression:
  - Scales captured high-resolution camera photo down to max 1280x720.
  - Compresses to JPEG quality 0.7 (resulting file size strictly **< 150 KB**).
  - Preserves original GPS metadata and capture timestamp.
- [ ] Verify image compression executes smoothly on low-spec Android devices without crashing mobile browser memory.

**Deliverable:** Proof-of-delivery photos compressed to < 150 KB on the device before upload.

---

## Thursday — `IndexedDB` Offline Event Queue & Background Sync

- [ ] Implement offline storage layer using browser `IndexedDB`:
  - When driver marks a milestone (e.g., `pickup_arrived`, `goods_checked`, `arrived_destination`) while offline:
    - Capture device GPS latitude/longitude.
    - Record exact `device_captured_at` ISO timestamp.
    - Queue event in local `IndexedDB`.
- [ ] Implement auto-sync worker:
  - Listens for browser `online` event.
  - Batches queued offline events into `POST /v1/driver/jobs/{id}/events`.
- [ ] Backend records both `device_captured_at` and `server_received_at` per Section 20.6 (delays are visible but not penalized as fraud).

**Deliverable:** Complete offline-first execution; drivers can operate in underground market basements without losing status history.

---

## Friday — Handover OTP Verification & Job Finalization

- [ ] Implement Handover Completion UI:
  - Input field for recipient 6-digit OTP code (buyer receives OTP on WhatsApp/SMS).
  - Mandatory upload of compressed proof-of-delivery photo.
  - Delivery notes text area.
- [ ] Implement `POST /v1/driver/jobs/{id}/complete`:
  - Validates recipient OTP against hashed `handover_otp_hash`.
  - Transitions `DeliveryJob` to `delivered` and `Order` to `DeliveredPendingConfirmation`.
- [ ] Trigger automated notification to buyer to confirm receipt or report discrepancies.

**Deliverable:** Secure physical delivery handover verified via recipient OTP and photographic proof.

---

## Weekend / Catch-Up
- Conduct field testing in physical markets with spotty cellular reception.
- Prepare route exception reason codes and levy approval workflows for Week 11.
