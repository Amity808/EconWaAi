-- ==============================================================================
-- V7: Compliance, Tokenized Identity Verifications, Informal Trader & Guarantors
-- (NDPA 2023 & Section 20 Anti-Fraud Safeguards)
-- ==============================================================================

-- 1. Tokenized Identity Verifications Table (Zero-Raw Storage)
CREATE TABLE IF NOT EXISTS identity_verifications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id              UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_type             TEXT NOT NULL,                 -- 'NIN', 'BVN', 'CAC'
    identifier_hash     TEXT NOT NULL,                 -- SHA-256 hash (never store raw NIN/BVN)
    provider_reference  TEXT NOT NULL,                 -- Aggregator reference (Prembly/Smile ID)
    status              TEXT NOT NULL,                 -- 'verified', 'rejected', 'pending_liveness'
    liveness_score      NUMERIC(4, 3),                 -- 0.000 to 1.000
    verified_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_identity_verif_hash ON identity_verifications(identifier_hash);
CREATE INDEX IF NOT EXISTS idx_identity_verif_org ON identity_verifications(org_id);

-- 2. Informal Trader Alternative Verification & Guarantor Table
CREATE TABLE IF NOT EXISTS informal_trader_profiles (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id                          UUID NOT NULL UNIQUE REFERENCES organizations(id) ON DELETE CASCADE,
    market_name                     TEXT NOT NULL,
    trade_association_name          TEXT NOT NULL,
    trade_association_reg_number   TEXT,
    market_leader_name              TEXT NOT NULL,
    market_leader_phone             TEXT NOT NULL,
    market_leader_endorsement_letter_url TEXT,
    guarantor_full_name             TEXT NOT NULL,
    guarantor_phone                 TEXT NOT NULL,
    guarantor_nin_hash              TEXT,
    guarantor_bvn_hash              TEXT,
    verification_status             TEXT NOT NULL DEFAULT 'pending_review',
    reviewed_by_admin_id            UUID REFERENCES users(id),
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_informal_trader_org ON informal_trader_profiles(org_id);

-- 3. Bank Account Change Audit Log (48-Hour Payout Hold Tracking)
CREATE TABLE IF NOT EXISTS bank_account_audit_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id              UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    changed_by_user_id  UUID NOT NULL REFERENCES users(id),
    old_account_number  TEXT,
    new_account_number  TEXT NOT NULL,
    old_bank_code       TEXT,
    new_bank_code       TEXT NOT NULL,
    payout_hold_until   TIMESTAMPTZ NOT NULL,
    notification_sent   BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_bank_audit_org ON bank_account_audit_logs(org_id);
