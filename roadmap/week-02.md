# Week 2 — Identity, Consents & Multi-Party Tenancy

> **Goal:** Build the multi-party authentication engine, implement PostgreSQL Row-Level Security (RLS) enforcement via Spring interceptors, and deliver NDPA 2023 compliant tokenized verification flows.

---

## Monday — JWT Authentication & Phone-Based OTP

- [ ] Implement `JwtTokenService` using `io.jsonwebtoken:jjwt` (generating 15-minute access tokens and 30-day refresh tokens).
- [ ] Implement `POST /v1/auth/request-otp` and `POST /v1/auth/verify-otp` for phone-based logins.
- [ ] Connect OTP generation to Redis with 5-minute TTL and rate limiting (max 3 attempts per phone number).
- [ ] Integrate Termii SMS client for fallback transactional OTP delivery.
- [ ] Write unit tests for JWT creation, claims validation, and expiration.

**Deliverable:** Phone numbers can authenticate via OTP and receive cryptographically signed JWTs containing `org_id`, `org_type`, and `role`.

---

## Tuesday — PostgreSQL Row-Level Security (RLS) Enforcement

- [ ] Write Flyway migration `V6__enable_row_level_security.sql`:
  - Enable RLS on `orders`, `rfqs`, `quotations`, `delivery_jobs`, `bank_accounts`.
  - Create tenant-scoping policies reading from `current_setting('app.current_org_id', true)`.
- [ ] Implement `TenantAwareDataSourceInterceptor` in `econwa-infrastructure`:
  - Automatically executes `SET LOCAL app.current_org_id = ?` whenever a connection is checked out of the HikariCP pool.
- [ ] Implement `JwtAuthenticationFilter` in Spring Security to extract JWT claims into `OrgContextHolder`.
- [ ] Write automated integration tests verifying that Organization A cannot query records belonging to Organization B.

**Deliverable:** Database kernel enforces tenant data isolation automatically on every query.

---

## Wednesday — NDPA 2023 Consents & Identity Tokenization

- [ ] Implement `ConsentService` in `econwa-core`:
  - Enforce mandatory explicit consent recording before passing any data to external verification partners.
  - Implement `POST /v1/compliance/consent` recording IP, channel (WhatsApp/Web), and legal consent text.
- [ ] Implement `IdentityVerificationGateway` (Prembly / Smile ID integration):
  - Tokenized NIN verification interface.
  - Strictly enforce **zero-raw storage** rule (persist only provider verification reference and one-way cryptographic hash).
- [ ] Implement 3D selfie liveness check callback handler for high-risk roles (suppliers receiving payouts, Tier 3 transporters).

**Deliverable:** Privacy compliance engine live; NIN verification completed without storing raw national identifiers.

---

## Thursday — CAC Corporate Verification & Guarantor Models

- [ ] Implement CAC corporate lookup service validating RC/BN registered business names and directors.
- [ ] Implement informal trader alternative verification workflow (trade association reference, market leader endorsement, guarantor details).
- [ ] Add payout hold trigger: any update to company bank account details automatically activates `payout_hold_until = now() + INTERVAL '48 hours'`.
- [ ] Implement admin notification when an account detail change occurs.

**Deliverable:** Corporate and informal verification pipelines active with automated 48-hour anti-fraud payout freezes.

---

## Friday — End-to-End Auth & Compliance Testing

- [ ] Write end-to-end integration tests covering:
  - Phone OTP request -> verification -> JWT emission.
  - Tenant context propagation to database RLS policies.
  - Consent recording followed by tokenized identity check.
  - Fraud test: attempted cross-tenant data access yields empty results.
- [ ] Build tokenized single-use webview URL generator for WhatsApp KYC handoff.

**Deliverable:** Multi-party identity and tenant isolation engine fully verified and regression-tested.

---

## Weekend / Catch-Up
- Verify webhook callbacks from identity verification sandbox.
- Finalize WhatsApp Business Platform template submissions with Meta.
