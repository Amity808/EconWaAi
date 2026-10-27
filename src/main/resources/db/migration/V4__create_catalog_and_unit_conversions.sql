-- ==============================================================================
-- V4: Product Catalog, Unit Conversions, Synonyms and Price History (Section 20.3, 20.5 & 20.11)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS products (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    canonical_name      TEXT NOT NULL,                 -- e.g., "Parboiled Rice", "White Garri"
    category            TEXT NOT NULL,                 -- 'grains', 'tubers', 'fmcg', 'oils', 'spices'
    standard_unit       TEXT NOT NULL,                 -- 'kg', 'litre', 'item'
    description         TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS unit_conversions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    local_trade_unit    TEXT NOT NULL,                 -- '50kg_bag', '25kg_bag', 'mudu', 'derica', 'paint_rubber', 'carton'
    multiplier          NUMERIC(10, 4) NOT NULL,       -- Ratio to product.standard_unit (e.g., 50.0 for 50kg bag)
    market_name         TEXT,                          -- Optional: specific market variation
    supplier_org_id     UUID REFERENCES organizations(id), -- Optional: supplier custom packing
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_unit_conversion_unique ON unit_conversions(
    product_id,
    local_trade_unit,
    COALESCE(market_name, ''),
    COALESCE(supplier_org_id, '00000000-0000-0000-0000-000000000000'::UUID)
);

CREATE TABLE IF NOT EXISTS product_synonyms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    synonym             TEXT NOT NULL,                 -- e.g., "Mama Gold", "Royal Stallion", "Garri Ijebu"
    language_or_dialect TEXT DEFAULT 'en_NG',          -- 'pcm' (Pidgin), 'yo' (Yoruba), 'ha' (Hausa), 'ig' (Igbo)
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_product_synonyms_trgm ON product_synonyms USING GIN (synonym gin_trgm_ops);

CREATE TABLE IF NOT EXISTS price_history (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL REFERENCES products(id),
    supplier_org_id     UUID NOT NULL REFERENCES organizations(id),
    unit_price_kobo     BIGINT NOT NULL,
    trade_unit          TEXT NOT NULL,
    recorded_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_price_history_lookup ON price_history(product_id, supplier_org_id, recorded_at DESC);
