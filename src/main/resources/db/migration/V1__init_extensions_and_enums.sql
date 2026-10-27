-- ==============================================================================
-- V1: Extensions and Base Enums
-- ==============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

DO $$ BEGIN
    CREATE TYPE org_type_enum AS ENUM (
        'buyer',
        'supplier',
        'transporter',
        'financial_partner',
        'platform_admin'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE kyc_status_enum AS ENUM (
        'unverified',
        'tier_1_basic',
        'tier_2_nin_cac',
        'tier_3_liveness_inspected'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE user_role_enum AS ENUM (
        'owner',
        'manager',
        'procurement_officer',
        'sales_rep',
        'dispatcher',
        'driver',
        'finance_officer',
        'ops_agent'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;
