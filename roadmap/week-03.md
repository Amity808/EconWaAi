# Week 3 — WhatsApp Gateway & Self-Service Onboarding

> **Goal:** Build the Meta WhatsApp Business Cloud API integration, implement webhook HMAC verification, and deliver complete conversational self-service onboarding for Buyers, Sellers/Suppliers, and Transporters.

---

## Monday — WhatsApp Cloud API Ingestion & Webhook Verification

- [ ] Implement `WhatsAppCloudApiClient` using Spring `RestClient` (calling Meta Graph API v21.0).
- [ ] Implement `POST /v1/webhooks/whatsapp`:
  - Handle Meta webhook verification challenge (`hub.mode`, `hub.challenge`, `hub.verify_token`).
  - Implement `X-Hub-Signature-256` HMAC-SHA256 signature verification filter using `MessageDigest.isEqual`.
- [ ] Connect webhook ingestion to Redis Queue to decouple incoming bursts from background processing.
- [ ] Implement message deduplication using atomic Redis key locks (`wamid` message IDs).

**Deliverable:** Secure WhatsApp webhook endpoint receiving messages with sub-100ms response to Meta.

---

## Tuesday — Conversational State Machine & Interactive Menus

- [ ] Implement `WhatsAppSessionManager` in Redis (caching conversational step, user role, and draft state with 24-hour TTL).
- [ ] Build interactive message builder for WhatsApp:
  - Interactive Button Messages (`interactive.type = "button"`).
  - Interactive List Messages (`interactive.type = "list"`).
- [ ] Implement initial message router:
  - Unregistered numbers receive a welcome message with role selection:
    `[ 🛒 Wholesale Buyer ]`, `[ 📦 Wholesale Supplier ]`, `[ 🚚 Transporter ]`.
- [ ] Write unit tests for interactive payload generation and state transitions.

**Deliverable:** First-time WhatsApp users receive an interactive welcome menu and can choose their role.

---

## Wednesday — Conversational Profile Setup & Location Engine

- [ ] Implement conversational business profile collection:
  - Capture Legal / Trade Name (e.g., *"Alaba Electronics & Provisions"*).
- [ ] Implement landmark-based location capture over WhatsApp:
  - Ask for Market Name (e.g., *"Alaba International Market"*, *"Bodija"*).
  - Ask for Market Line / Section and Shop Number (e.g., *"Fancy Line, Block C, Shop 44"*).
  - Capture descriptive landmark text (e.g., *"Opposite Central Mosque, enter black gate"*).
- [ ] Implement native WhatsApp GPS Location pin receiver:
  - Extract latitude, longitude, and accuracy from WhatsApp `type: "location"` message.
  - Persist into `organization_locations`.
- [ ] Capture on-site stall manager contact name and phone number.

**Deliverable:** Users can register their physical market shop and drop their exact GPS pin over WhatsApp.

---

## Thursday — Role-Specific Onboarding Workflows

- [ ] Implement **Wholesale Buyer** flow completion:
  - Set default delivery destination; present instant voice RFQ instructions.
- [ ] Implement **Wholesale Supplier** flow:
  - Select primary commodity categories (Grains, FMCG, Oils, Tubers).
  - Prompt for initial catalogue setup or invoice photo upload.
- [ ] Implement **Transporter** flow:
  - Select fleet vehicle classes (vans, 5-tonne, 10-tonne trucks).
  - Select primary transit corridors (e.g., Lagos–Ibadan, Alaba–Trade Fair).
- [ ] Implement tokenized KYC link generation sent via WhatsApp to complete NIN/CAC check.

**Deliverable:** All three ecosystem personas can complete customized WhatsApp onboarding flows.

---

## Friday — End-to-End Onboarding Testing & Verification

- [ ] Conduct end-to-end sandbox testing using live WhatsApp test numbers:
  - Simulate Buyer registration and pin drop.
  - Simulate Supplier registration and category selection.
  - Simulate Transporter registration and corridor setup.
- [ ] Verify that database records are properly created in `organizations`, `organization_locations`, and `users`.
- [ ] Verify fallback SMS trigger if WhatsApp session cannot be established.

**Deliverable:** Complete WhatsApp-first onboarding operational in staging.

---

## Weekend / Catch-Up
- Test edge cases: user sends voice note during onboarding, user sends invalid location message.
- Prepare audio training samples for Week 4 Faster-Whisper pipeline.
