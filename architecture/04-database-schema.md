# 04 — Database Schema

## 1. Schema Conventions & Rules

- **Primary Keys**: `UUID` generated via `gen_random_uuid()` (v4/v7).
- **Currency & Money**: **Always `BIGINT` in kobo** (₦1 = 100 kobo). Floating-point and arbitrary precision decimals are strictly forbidden for financial balances.
- **Timestamps**: `TIMESTAMPTZ` defaults to `now()` in UTC. Local time display uses `Africa/Lagos`.
- **Soft Deletes**: `deleted_at TIMESTAMPTZ` (NULL indicates active record).
- **Audit Columns**: Every table includes `created_at` and `updated_at`.
- **Foreign Keys**: Explicit `ON DELETE RESTRICT` for financial tables; `ON DELETE CASCADE` only for ephemeral child relations.

---

## 2. Organizations, Locations & Users

### 2.1 `organizations`
```sql
CREATE TYPE org_type_enum AS ENUM ('buyer', 'supplier', 'transporter', 'financial_partner', 'platform_admin');
CREATE TYPE kyc_status_enum AS ENUM ('unverified', 'tier_1_basic', 'tier_2_nin_cac', 'tier_3_liveness_inspected');

CREATE TABLE organizations (
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

CREATE UNIQUE INDEX idx_organizations_cac ON organizations(cac_number) WHERE cac_number IS NOT NULL;
CREATE INDEX idx_organizations_type ON organizations(org_type);
```

### 2.2 `organization_locations` (Section 20.2 & 20.11)
```sql
CREATE TABLE organization_locations (
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
    operating_hours         JSONB DEFAULT '{"open": "07:00", "close": "18:00", "closed_days": [0]}',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_org_locations_org ON organization_locations(org_id);
```

### 2.3 `users` & `consents` (Section 20.7 & 20.11)
```sql
CREATE TYPE user_role_enum AS ENUM ('owner', 'manager', 'procurement_officer', 'sales_rep', 'dispatcher', 'driver', 'finance_officer', 'ops_agent');

CREATE TABLE users (
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

CREATE TABLE consents (
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

CREATE INDEX idx_consents_org_type ON consents(org_id, consent_type);
```

---

## 3. Product Catalog, Synonyms & Local Trade Units

### 3.1 `products` & `unit_conversions` (Section 20.5 & 20.11)
```sql
CREATE TABLE products (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    canonical_name      TEXT NOT NULL,                 -- e.g., "Parboiled Rice", "White Garri"
    category            TEXT NOT NULL,                 -- 'grains', 'tubers', 'fmcg', 'oils', 'spices'
    standard_unit       TEXT NOT NULL,                 -- 'kg', 'litre', 'item'
    description         TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE unit_conversions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    local_trade_unit    TEXT NOT NULL,                 -- '50kg_bag', '25kg_bag', 'mudu', 'derica', 'paint_rubber', 'carton'
    multiplier          NUMERIC(10, 4) NOT NULL,       -- Ratio to product.standard_unit (e.g., 50.0 for 50kg bag)
    market_name         TEXT,                          -- Optional: specific market variation
    supplier_org_id     UUID REFERENCES organizations(id), -- Optional: supplier custom packing
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_unit_conversion_unique ON unit_conversions(product_id, local_trade_unit, COALESCE(market_name, ''), COALESCE(supplier_org_id, '00000000-0000-0000-0000-000000000000'::UUID));
```

### 3.2 `product_synonyms` & `price_history` (Section 20.3, 20.5 & 20.11)
```sql
CREATE TABLE product_synonyms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    synonym             TEXT NOT NULL,                 -- e.g., "Mama Gold", "Royal Stallion", "Garri Ijebu"
    language_or_dialect TEXT DEFAULT 'en_NG',          -- 'pcm' (Pidgin), 'yo' (Yoruba), 'ha' (Hausa), 'ig' (Igbo)
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_synonyms_trgm ON product_synonyms USING GIN (synonym gin_trgm_ops);

CREATE TABLE price_history (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL REFERENCES products(id),
    supplier_org_id     UUID NOT NULL REFERENCES organizations(id),
    unit_price_kobo     BIGINT NOT NULL,
    trade_unit          TEXT NOT NULL,
    recorded_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_price_history_lookup ON price_history(product_id, supplier_org_id, recorded_at DESC);

### 3.3 `supplier_catalog_items` (Registered Supplier Catalogues)
```sql
CREATE TYPE stock_status_enum AS ENUM ('in_stock', 'low_stock', 'out_of_stock');

CREATE TABLE supplier_catalog_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_org_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    product_id          UUID NOT NULL REFERENCES products(id),
    custom_product_name TEXT,                          -- Supplier's trade/brand packaging name
    trade_unit          TEXT NOT NULL,                 -- e.g., '50kg_bag', 'carton'
    moq                 INT NOT NULL DEFAULT 1,        -- Minimum Order Quantity
    indicative_price_kobo BIGINT,                      -- Base guide price (subject to valid_until quote)
    stock_status        stock_status_enum NOT NULL DEFAULT 'in_stock',
    available_quantity  INT,                           -- Optional live inventory count
    pickup_location_id  UUID NOT NULL REFERENCES organization_locations(id),
    is_active           BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_supplier_catalog_lookup ON supplier_catalog_items(supplier_org_id, product_id, is_active);
CREATE INDEX idx_supplier_catalog_product ON supplier_catalog_items(product_id, is_active);
CREATE INDEX idx_supplier_catalog_pickup ON supplier_catalog_items(pickup_location_id);
```

---

## 4. RFQ, Quotations & Order Engine

### 4.1 `rfqs` & `quotations` (Section 20.3 & 20.11)
```sql
CREATE TYPE rfq_status_enum AS ENUM ('draft', 'parsing', 'pending_quotes', 'quotes_received', 'ordered', 'expired', 'cancelled');
CREATE TYPE quote_status_enum AS ENUM ('submitted', 'accepted', 'rejected', 'expired');

CREATE TABLE rfqs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_org_id        UUID NOT NULL REFERENCES organizations(id),
    channel             TEXT NOT NULL,                 -- 'whatsapp_voice', 'whatsapp_text', 'web'
    raw_audio_url       TEXT,
    transcription_text  TEXT,
    extracted_items     JSONB NOT NULL DEFAULT '[]',   -- [{product_id, quantity, unit, confidence}]
    ai_confidence_score NUMERIC(4, 3),                 -- 0.000 to 1.000
    destination_loc_id  UUID NOT NULL REFERENCES organization_locations(id),
    status              rfq_status_enum NOT NULL DEFAULT 'pending_quotes',
    expires_at          TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE quotations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rfq_id              UUID NOT NULL REFERENCES rfqs(id),
    supplier_org_id     UUID NOT NULL REFERENCES organizations(id),
    pickup_loc_id       UUID NOT NULL REFERENCES organization_locations(id),
    items               JSONB NOT NULL,                -- [{product_id, unit_price_kobo, qty, trade_unit, subtotal_kobo}]
    total_amount_kobo   BIGINT NOT NULL,
    valid_until         TIMESTAMPTZ NOT NULL,          -- Explicit price guarantee window
    status              quote_status_enum NOT NULL DEFAULT 'submitted',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_quotations_rfq ON quotations(rfq_id, status);
CREATE INDEX idx_quotations_validity ON quotations(valid_until);
```

### 4.2 `orders` & `payment_terms` (Section 20.1, 20.3 & 20.11)
```sql
CREATE TYPE order_status_enum AS ENUM (
    'created',
    'pending_supplier_reconfirmation',  -- Step 9 reconfirmation
    'awaiting_payment',
    'paid_escrow',
    'dispatched',
    'in_transit',
    'delivered_pending_confirmation',
    'completed',
    'disputed',
    'cancelled'
);

CREATE TYPE payment_term_type_enum AS ENUM ('pay_before_dispatch', 'pay_on_delivery', 'part_payment', 'supplier_credit', 'procureai_credit');

CREATE TABLE payment_terms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    term_type           payment_term_type_enum NOT NULL,
    deposit_kobo        BIGINT DEFAULT 0,              -- Used for part_payment
    balance_due_days    INT DEFAULT 0,                 -- 7, 14, 28 days
    balance_due_date    TIMESTAMPTZ,
    supplier_agreed     BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number        TEXT NOT NULL UNIQUE,          -- e.g., "ORD-202610-8472"
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
    reconfirmed_price   BIGINT,                        -- If reconfirmation modified price
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_buyer ON orders(buyer_org_id, status);
CREATE INDEX idx_orders_supplier ON orders(supplier_org_id, status);
```

---

## 5. Bank Accounts, Virtual NUBANs & Reconciliation

### 5.1 `bank_accounts` & `bank_transactions` (Section 20.1.1 & 20.11)
```sql
CREATE TYPE account_status_enum AS ENUM ('active', 'frozen', 'closed');

CREATE TABLE bank_accounts (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id                  UUID NOT NULL REFERENCES organizations(id),
    order_id                UUID REFERENCES orders(id), -- If dedicated virtual account per order
    partner_bank_code       TEXT NOT NULL,              -- e.g., 'MFB_001'
    account_number          TEXT NOT NULL,              -- 10-digit NUBAN
    account_name            TEXT NOT NULL,
    is_dedicated_virtual    BOOLEAN NOT NULL DEFAULT true,
    kyc_tier                INT NOT NULL DEFAULT 1,
    daily_inflow_limit_kobo BIGINT,
    status                  account_status_enum NOT NULL DEFAULT 'active',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_bank_accounts_nuban ON bank_accounts(account_number);
CREATE INDEX idx_bank_accounts_order ON bank_accounts(order_id);

CREATE TABLE bank_transactions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_account_id         UUID NOT NULL REFERENCES bank_accounts(id),
    session_id              TEXT NOT NULL UNIQUE,       -- Bank transfer session ID
    provider_reference      TEXT NOT NULL UNIQUE,       -- Webhook transaction reference
    amount_kobo             BIGINT NOT NULL,
    sender_account_number   TEXT,
    sender_name             TEXT,
    sender_bank             TEXT,
    webhook_payload         JSONB NOT NULL,             -- Raw verified webhook
    reconciled_with_statement BOOLEAN NOT NULL DEFAULT false,
    statement_reconciled_at TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_bank_tx_account ON bank_transactions(bank_account_id);
```

---

## 6. ProcureAI Credit, Points & Trade Scores

### 6.1 `trade_scores` & `points_ledger` (Section 20.1.2 & 20.11)
```sql
CREATE TYPE credit_tier_enum AS ENUM ('starter', 'bronze', 'silver', 'gold');

CREATE TABLE trade_scores (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_org_id        UUID NOT NULL REFERENCES organizations(id),
    score               INT NOT NULL CHECK (score BETWEEN 0 AND 1000),
    tier                credit_tier_enum NOT NULL,
    completed_orders    INT NOT NULL DEFAULT 0,
    ontime_repayments   INT NOT NULL DEFAULT 0,
    dispute_count       INT NOT NULL DEFAULT 0,
    points_balance      INT NOT NULL DEFAULT 0,
    score_breakdown     JSONB NOT NULL DEFAULT '{}',
    calculated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_trade_scores_buyer ON trade_scores(buyer_org_id);

CREATE TABLE points_ledger (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_org_id        UUID NOT NULL REFERENCES organizations(id),
    order_id            UUID REFERENCES orders(id),
    points_delta        INT NOT NULL,                  -- +10 for order, +25 for repayment, -30 for default
    reason              TEXT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### 6.2 `credit_lines`, `credit_drawdowns` & `repayments` (Section 20.1.2 & 20.11)
```sql
CREATE TABLE credit_lines (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_org_id        UUID NOT NULL REFERENCES organizations(id),
    lender_org_id       UUID NOT NULL REFERENCES organizations(id), -- Partner MFB
    credit_tier         credit_tier_enum NOT NULL,
    limit_kobo          BIGINT NOT NULL,
    used_kobo           BIGINT NOT NULL DEFAULT 0,
    is_frozen           BOOLEAN NOT NULL DEFAULT false,
    freeze_reason       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE credit_drawdowns (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    credit_line_id          UUID NOT NULL REFERENCES credit_lines(id),
    order_id                UUID NOT NULL REFERENCES orders(id),
    supplier_account_id     UUID NOT NULL REFERENCES bank_accounts(id), -- Payout destination (supplier only)
    principal_kobo          BIGINT NOT NULL,
    interest_kobo           BIGINT NOT NULL,
    fee_kobo                BIGINT NOT NULL,
    total_repayable_kobo    BIGINT NOT NULL,
    due_date                TIMESTAMPTZ NOT NULL,
    acceptance_signature    TEXT NOT NULL,              -- OTP or secure link proof
    accepted_at             TIMESTAMPTZ NOT NULL,
    status                  TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'repaid', 'overdue', 'defaulted'))
);

CREATE TABLE repayments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    drawdown_id         UUID NOT NULL REFERENCES credit_drawdowns(id),
    amount_kobo         BIGINT NOT NULL,
    bank_tx_id          UUID REFERENCES bank_transactions(id),
    late_charges_kobo   BIGINT NOT NULL DEFAULT 0,
    paid_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

---

## 7. Logistics, Route Rules & Exceptions

### 7.1 `delivery_jobs` & `route_rules` (Section 20.4 & 20.11)
```sql
CREATE TYPE delivery_job_status_enum AS ENUM (
    'broadcast',
    'assigned',
    'driver_en_route_pickup',
    'goods_loaded',
    'in_transit',
    'arrived_destination',
    'delivered',
    'exception_raised',
    'cancelled'
);

CREATE TABLE route_rules (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    corridor_name           TEXT NOT NULL,              -- e.g., "Lagos-Ibadan Expressway", "Apapa-Mile 2"
    access_restrictions     JSONB DEFAULT '[]',         -- Time-of-day truck bans
    rainy_season_active     BOOLEAN NOT NULL DEFAULT false,
    known_official_tolls_kobo BIGINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE delivery_jobs (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID NOT NULL REFERENCES orders(id),
    transporter_org_id      UUID REFERENCES organizations(id),
    assigned_driver_id      UUID REFERENCES users(id),
    pickup_loc_id           UUID NOT NULL REFERENCES organization_locations(id),
    delivery_loc_id         UUID NOT NULL REFERENCES organization_locations(id),
    agreed_fee_kobo         BIGINT NOT NULL,
    handover_otp_hash       TEXT NOT NULL,              -- 6-digit OTP entered by driver on delivery
    status                  delivery_job_status_enum NOT NULL DEFAULT 'broadcast',
    dispatched_at           TIMESTAMPTZ,
    delivered_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### 7.2 `delivery_events` & `delivery_exceptions` (Section 20.4 & 20.11)
```sql
CREATE TYPE exception_reason_enum AS ENUM (
    'levy_or_toll_demand',
    'checkpoint_delay',
    'flooding_or_impassable_road',
    'vehicle_access_restriction',
    'market_congestion',
    'security_concern_route_changed',
    'location_not_found_after_call'
);

CREATE TABLE delivery_events (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id              UUID NOT NULL REFERENCES delivery_jobs(id) ON DELETE CASCADE,
    event_type          TEXT NOT NULL,                 -- 'pickup_arrived', 'driver_called_recipient', 'goods_checked', 'otp_verified'
    latitude            NUMERIC(10, 7),
    longitude           NUMERIC(10, 7),
    device_captured_at  TIMESTAMPTZ NOT NULL,          -- Offline device timestamp
    server_received_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE delivery_exceptions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id              UUID NOT NULL REFERENCES delivery_jobs(id),
    reason              exception_reason_enum NOT NULL,
    notes               TEXT,
    extra_cost_kobo     BIGINT DEFAULT 0,
    is_approved_by_ops  BOOLEAN NOT NULL DEFAULT false,
    approved_by_user_id UUID REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

---

## 8. Financials, Tax & Messaging Costs

### 8.1 `tax_profiles` & `message_costs` (Section 20.9, 20.10 & 20.11)
```sql
CREATE TABLE tax_profiles (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id              UUID NOT NULL UNIQUE REFERENCES organizations(id),
    tin                 TEXT,
    is_vat_registered   BOOLEAN NOT NULL DEFAULT false,
    vat_rate_basis_pts  INT NOT NULL DEFAULT 750,      -- 7.50% (750 basis points)
    exemption_category  TEXT,                          -- 'basic_foodstuffs', 'zero_rated_export'
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE message_costs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID REFERENCES orders(id),
    recipient_phone     TEXT NOT NULL,
    template_category   TEXT NOT NULL,                 -- 'utility', 'marketing', 'authentication', 'service_free_window'
    meta_cost_kobo      BIGINT NOT NULL,               -- WhatsApp template charge in kobo
    sent_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_message_costs_order ON message_costs(order_id);
```
