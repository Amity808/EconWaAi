-- ==============================================================================
-- V6: Multi-Party Transaction Tables & Row-Level Security (RLS) Policies
-- (Section 03 Multi-Tenancy & Section 04 Database Schema)
-- ==============================================================================

-- 1. Create Enums for Orders, RFQs, Quotes, Bank Accounts, and Delivery Jobs
DO $$ BEGIN
    CREATE TYPE rfq_status_enum AS ENUM (
        'draft', 'parsing', 'pending_quotes', 'quotes_received', 'ordered', 'expired', 'cancelled'
    );
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE quote_status_enum AS ENUM (
        'submitted', 'accepted', 'rejected', 'expired'
    );
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE payment_term_type_enum AS ENUM (
        'pay_before_dispatch', 'pay_on_delivery', 'part_payment', 'supplier_credit', 'procureai_credit'
    );
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE order_status_enum AS ENUM (
        'created', 'pending_supplier_reconfirmation', 'awaiting_payment', 'paid_escrow',
        'dispatched', 'in_transit', 'delivered_pending_confirmation', 'completed', 'disputed', 'cancelled'
    );
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE account_status_enum AS ENUM (
        'active', 'frozen', 'closed'
    );
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE delivery_job_status_enum AS ENUM (
        'broadcast', 'assigned', 'driver_en_route_pickup', 'goods_loaded',
        'in_transit', 'arrived_destination', 'delivered', 'exception_raised', 'cancelled'
    );
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- 2. Create RFQ & Quotation Tables
CREATE TABLE IF NOT EXISTS rfqs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_org_id        UUID NOT NULL REFERENCES organizations(id),
    channel             TEXT NOT NULL,
    raw_audio_url       TEXT,
    transcription_text  TEXT,
    extracted_items     JSONB NOT NULL DEFAULT '[]',
    ai_confidence_score NUMERIC(4, 3),
    destination_loc_id  UUID NOT NULL REFERENCES organization_locations(id),
    status              rfq_status_enum NOT NULL DEFAULT 'pending_quotes',
    expires_at          TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS quotations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rfq_id              UUID NOT NULL REFERENCES rfqs(id),
    supplier_org_id     UUID NOT NULL REFERENCES organizations(id),
    pickup_loc_id       UUID NOT NULL REFERENCES organization_locations(id),
    items               JSONB NOT NULL DEFAULT '[]',
    total_amount_kobo   BIGINT NOT NULL,
    valid_until         TIMESTAMPTZ NOT NULL,
    status              quote_status_enum NOT NULL DEFAULT 'submitted',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS payment_terms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    term_type           payment_term_type_enum NOT NULL,
    deposit_kobo        BIGINT DEFAULT 0,
    balance_due_days    INT DEFAULT 0,
    balance_due_date    TIMESTAMPTZ,
    supplier_agreed     BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. Create Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number        TEXT NOT NULL UNIQUE,
    rfq_id              UUID REFERENCES rfqs(id),
    quotation_id        UUID REFERENCES quotations(id),
    buyer_org_id        UUID NOT NULL REFERENCES organizations(id),
    supplier_org_id     UUID NOT NULL REFERENCES organizations(id),
    pickup_loc_id       UUID NOT NULL REFERENCES organization_locations(id),
    delivery_loc_id     UUID NOT NULL REFERENCES organization_locations(id),
    payment_terms_id    UUID NOT NULL REFERENCES payment_terms(id),
    goods_subtotal_kobo BIGINT NOT NULL,
    logistics_fee_kobo  BIGINT NOT NULL DEFAULT 0,
    platform_fee_kobo   BIGINT NOT NULL DEFAULT 0,
    vat_kobo            BIGINT NOT NULL DEFAULT 0,
    total_amount_kobo   BIGINT NOT NULL,
    amount_paid_kobo    BIGINT NOT NULL DEFAULT 0,
    status              order_status_enum NOT NULL DEFAULT 'created',
    step_9_reconfirmed  BOOLEAN NOT NULL DEFAULT false,
    reconfirmed_price   BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 4. Create Bank Accounts & Delivery Jobs
CREATE TABLE IF NOT EXISTS bank_accounts (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id                  UUID NOT NULL REFERENCES organizations(id),
    order_id                UUID REFERENCES orders(id),
    partner_bank_code       TEXT NOT NULL,
    account_number          TEXT NOT NULL UNIQUE,
    account_name            TEXT NOT NULL,
    is_dedicated_virtual    BOOLEAN NOT NULL DEFAULT true,
    kyc_tier                INT NOT NULL DEFAULT 1,
    daily_inflow_limit_kobo BIGINT,
    status                  account_status_enum NOT NULL DEFAULT 'active',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS delivery_jobs (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID NOT NULL REFERENCES orders(id),
    transporter_org_id      UUID REFERENCES organizations(id),
    assigned_driver_id      UUID REFERENCES users(id),
    pickup_loc_id           UUID NOT NULL REFERENCES organization_locations(id),
    delivery_loc_id         UUID NOT NULL REFERENCES organization_locations(id),
    agreed_fee_kobo         BIGINT NOT NULL,
    handover_otp_hash       TEXT NOT NULL,
    status                  delivery_job_status_enum NOT NULL DEFAULT 'broadcast',
    dispatched_at           TIMESTAMPTZ,
    delivered_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 5. Enable Row-Level Security (RLS) on Tenant Tables
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE rfqs ENABLE ROW LEVEL SECURITY;
ALTER TABLE quotations ENABLE ROW LEVEL SECURITY;
ALTER TABLE delivery_jobs ENABLE ROW LEVEL SECURITY;
ALTER TABLE bank_accounts ENABLE ROW LEVEL SECURITY;

-- 6. Define PostgreSQL RLS Policies reading session variables

-- Orders Policy: Buyer, Supplier, or Platform Admin can view
DROP POLICY IF EXISTS orders_tenant_policy ON orders;
CREATE POLICY orders_tenant_policy ON orders
    FOR ALL
    USING (
        buyer_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID
        OR supplier_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID
        OR current_setting('app.current_org_type', true) = 'platform_admin'
        OR current_setting('app.current_org_id', true) IS NULL
    );

-- RFQs Policy: Buyer or Platform Admin can view
DROP POLICY IF EXISTS rfqs_tenant_policy ON rfqs;
CREATE POLICY rfqs_tenant_policy ON rfqs
    FOR ALL
    USING (
        buyer_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID
        OR current_setting('app.current_org_type', true) = 'platform_admin'
        OR current_setting('app.current_org_id', true) IS NULL
    );

-- Quotations Policy: Quoting Supplier, Purchasing Buyer (via RFQ), or Platform Admin can view
DROP POLICY IF EXISTS quotations_tenant_policy ON quotations;
CREATE POLICY quotations_tenant_policy ON quotations
    FOR ALL
    USING (
        supplier_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID
        OR EXISTS (SELECT 1 FROM rfqs r WHERE r.id = quotations.rfq_id AND r.buyer_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID)
        OR current_setting('app.current_org_type', true) = 'platform_admin'
        OR current_setting('app.current_org_id', true) IS NULL
    );

-- Bank Accounts Policy: Owner Organization or Platform Admin
DROP POLICY IF EXISTS bank_accounts_tenant_policy ON bank_accounts;
CREATE POLICY bank_accounts_tenant_policy ON bank_accounts
    FOR ALL
    USING (
        org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID
        OR current_setting('app.current_org_type', true) = 'platform_admin'
        OR current_setting('app.current_org_id', true) IS NULL
    );

-- Delivery Jobs Policy: Transporter, Driver, or Platform Admin
DROP POLICY IF EXISTS delivery_jobs_tenant_policy ON delivery_jobs;
CREATE POLICY delivery_jobs_tenant_policy ON delivery_jobs
    FOR ALL
    USING (
        transporter_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID
        OR assigned_driver_id = NULLIF(current_setting('app.current_user_id', true), '')::UUID
        OR (status = 'broadcast' AND current_setting('app.current_org_type', true) = 'transporter')
        OR current_setting('app.current_org_type', true) = 'platform_admin'
        OR current_setting('app.current_org_id', true) IS NULL
    );
