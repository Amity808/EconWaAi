-- ==============================================================================
-- V5: Registered Supplier Catalogues (Section 20.3 & 20.11)
-- ==============================================================================

DO $$ BEGIN
    CREATE TYPE stock_status_enum AS ENUM ('in_stock', 'low_stock', 'out_of_stock');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS supplier_catalog_items (
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

CREATE INDEX IF NOT EXISTS idx_supplier_catalog_lookup ON supplier_catalog_items(supplier_org_id, product_id, is_active);
CREATE INDEX IF NOT EXISTS idx_supplier_catalog_product ON supplier_catalog_items(product_id, is_active);
CREATE INDEX IF NOT EXISTS idx_supplier_catalog_pickup ON supplier_catalog_items(pickup_location_id);
