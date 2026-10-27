-- ==============================================================================
-- V2: Organizations and Locations (Section 20.2 & 20.11)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS organizations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legal_name          TEXT NOT NULL,
    trade_name          TEXT NOT NULL,
    org_type            org_type_enum NOT NULL,
    cac_number          TEXT,                     -- RC / BN number
    tin                 TEXT,                     -- Tax Identification Number
    kyc_status          kyc_status_enum NOT NULL DEFAULT 'unverified',
    kyc_verified_at     TIMESTAMPTZ,
    primary_phone       TEXT NOT NULL,
    primary_email       TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT true,
    payout_hold_until   TIMESTAMPTZ,              -- Set during bank detail changes
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_organizations_cac ON organizations(cac_number) WHERE cac_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_organizations_type ON organizations(org_type);

CREATE TABLE IF NOT EXISTS organization_locations (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id                  UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    label                   TEXT NOT NULL,             -- e.g., "Main Warehouse", "Alaba Shop"
    market_name             TEXT,                      -- e.g., "Alaba International Market", "Bodija"
    section_or_line         TEXT,                      -- e.g., "Fancy Line", "Block C"
    shop_or_plaza_number    TEXT,                      -- e.g., "Shop 44, Peace Plaza"
    landmark_description    TEXT NOT NULL,             -- e.g., "Opposite Central Mosque, yellow gate"
    latitude                NUMERIC(10, 7),
    longitude               NUMERIC(10, 7),
    gps_accuracy_meters     NUMERIC(6, 2),
    onsite_contact_name     TEXT NOT NULL,
    onsite_contact_phone    TEXT NOT NULL,
    is_default              BOOLEAN NOT NULL DEFAULT false,
    status                  TEXT NOT NULL DEFAULT 'active',
    is_active               BOOLEAN NOT NULL DEFAULT true,
    operating_hours         JSONB DEFAULT '{"open": "07:00", "close": "18:00", "closed_days": [0]}',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_org_locations_org ON organization_locations(org_id);
