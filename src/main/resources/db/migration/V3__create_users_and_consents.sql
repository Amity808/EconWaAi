-- ==============================================================================
-- V3: Users and Consents (Section 20.7 & 20.11 - NDPA 2023 Compliant)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id              UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    phone_number        TEXT NOT NULL UNIQUE,          -- Primary WhatsApp / login identifier
    full_name           TEXT NOT NULL,
    email               TEXT,
    password_hash       TEXT,                          -- NULL for OTP-only field users
    role                user_role_enum NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT true,
    last_login_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS consents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id),
    org_id              UUID NOT NULL REFERENCES organizations(id),
    consent_type        TEXT NOT NULL,                 -- 'bvn_verification', 'nin_validation', 'credit_bureau_check', 'auto_debit'
    consent_text        TEXT NOT NULL,
    granted_ip          TEXT,
    granted_channel     TEXT NOT NULL,                 -- 'whatsapp_otp', 'web_signature'
    granted_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at          TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_consents_org_type ON consents(org_id, consent_type);
CREATE INDEX IF NOT EXISTS idx_users_org ON users(org_id);
