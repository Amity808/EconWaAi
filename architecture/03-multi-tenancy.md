# 03 — Multi-Tenancy & Multi-Party Model

## 1. Multi-Party Ecosystem vs. Single-Merchant SaaS

Unlike standard single-merchant SaaS platforms, EconWaAi operates a **multi-party wholesale network**. The system orchestrates interactions between four distinct organization archetypes while enforcing strict cryptographic and logical boundaries between them:

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                            MULTI-PARTY PLATFORM ECOSYSTEM                        │
│                                                                                  │
│   ┌────────────────────┐   Orders / Credit     ┌─────────────────────────────┐   │
│   │ Buyer Organization │ ────────────────────▶ │   Supplier Organization     │   │
│   │ (Wholesale Retail) │                       │   (Distributor / Importer)  │   │
│   └─────────┬──────────┘                       └─────────────┬───────────────┘   │
│             │                                                │                   │
│             │ Delivery Job                                   │ Delivery Job      │
│             ▼                                                ▼                   │
│   ┌──────────────────────────────────────────────────────────────────────────┐   │
│   │                       Transporter Organization                           │   │
│   │             (Fleet Owner, Dispatcher, Independent Drivers)               │   │
│   └─────────────────────────────────────┬────────────────────────────────────┘   │
│                                         │                                        │
│                                         │ Settlement & Underwriting              │
│                                         ▼                                        │
│   ┌──────────────────────────────────────────────────────────────────────────┐   │
│   │                        Partner Microfinance Bank                         │   │
│   │               (Virtual Account Custody & BNPL Liquidity)                 │   │
│   └──────────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Organization Archetypes & Roles

### 2.1 Organization Types
Every registered entity in EconWaAi belongs to an `organizations` record with an assigned `org_type`:
- `buyer`: Places RFQs, requests quotes, orders inventory, holds a virtual bank account and credit lines.
- `supplier`: Publishes availability, submits binding quotations, fulfills orders, receives payouts.
- `transporter`: Operates commercial transport fleets, claims route jobs, assigns drivers.
- `financial_partner`: Partner MFB operating credit facilities, monitoring loan performance.
- `platform_admin`: EconWaAi operational oversight, dispute resolution, compliance audit.

### 2.2 User Roles & Permissions Matrix

| Role | Organization Scope | Permitted Actions |
|:---|:---|:---|
| **Org Owner** | Buyer / Supplier / Transporter | Full authority: manage members, modify company details, withdraw funds |
| **Procurement Officer** | Buyer | Create voice/text RFQs, review quotes, initiate orders |
| **Sales Manager** | Supplier | Submit quotes, reconfirm prices, manage catalog, schedule dispatches |
| **Dispatcher** | Transporter | Review available routes, accept jobs, assign drivers and vehicles |
| **Field Driver** | Transporter | View assigned route landmarks, update transit status, record OTP & POD |
| **Finance Officer** | Any | View bank transactions, reconcile statements, track tax & invoices |
| **Ops Agent** | Platform | Mediate disputes, approve checkpoint levy claims, review KYC documents |

---

## 3. Database Isolation Strategy: Row-Level Security (RLS)

EconWaAi enforces multi-tenancy at the **PostgreSQL kernel level** using Row-Level Security (RLS). Even if application code has a bug or omits a `WHERE` clause, the database prevents cross-organization data leakage.

### 3.1 Tenant Session Variables
Every database connection pulled from the connection pool is parameterized with the authenticated user’s context before query execution:

```sql
-- Set during Spring Security filter / DataSource connection checkout
SET LOCAL app.current_user_id = '018f4a32-721a-7b3e-91c2-552d7e012345';
SET LOCAL app.current_org_id = '018f4a32-721a-7b3e-91c2-552d7e067890';
SET LOCAL app.current_org_type = 'buyer';
SET LOCAL app.current_role = 'procurement_officer';
```

### 3.2 Dual-Party Order Access Policy Example
An order involves both a Buyer and a Supplier. The RLS policy allows read access to both parties, but restricts status modifications based on lifecycle state:

```sql
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

-- Both the purchasing buyer and the fulfilling supplier can view the order
CREATE POLICY orders_read_policy ON orders
    FOR SELECT
    USING (
        buyer_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID
        OR
        supplier_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID
        OR
        current_setting('app.current_org_type', true) = 'platform_admin'
    );

-- Buyers can cancel only before confirmation; Suppliers can confirm or reject
CREATE POLICY orders_update_policy ON orders
    FOR UPDATE
    USING (
        (buyer_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID 
         AND status IN ('draft', 'pending_quotes'))
        OR
        (supplier_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID 
         AND status IN ('pending_supplier_reconfirmation', 'processing'))
        OR
        current_setting('app.current_org_type', true) = 'platform_admin'
    );
```

### 3.3 Logistics Data Masking Policy
To prevent disintermediation and safeguard safety, full destination details (exact landmark, shop number, recipient phone) are **masked** from transporters and drivers until a job is formally assigned:

```sql
CREATE POLICY logistics_jobs_read_policy ON delivery_jobs
    FOR SELECT
    USING (
        -- Transporters see general route info for open broadcast jobs
        (status = 'broadcast' AND current_setting('app.current_org_type', true) = 'transporter')
        OR
        -- Assigned transporter sees full operational details
        transporter_org_id = NULLIF(current_setting('app.current_org_id', true), '')::UUID
        OR
        -- Assigned driver sees specific assigned task
        assigned_driver_id = NULLIF(current_setting('app.current_user_id', true), '')::UUID
        OR
        current_setting('app.current_org_type', true) = 'platform_admin'
    );
```

---

## 4. Multi-Location & Market Branch Isolation

Wholesale operations in Nigeria operate across multiple stalls and warehouses (e.g., Alaba International, Trade Fair Complex, Bodija Market):
- Organizations have one or more `organization_locations`.
- Orders and delivery jobs pin specific pickup and drop-off `location_id`s.
- Staff members can be restricted to specific locations to prevent cross-warehouse leakage.

---

## 5. Spring Security Filter & Tenant Context Pipeline

```
HTTP Request
    │
    ▼
[JwtAuthenticationFilter] ── Extract & Validate Bearer Token
    │
    ▼
[OrgContextHolder (ThreadLocal)] ── Store active org_id, org_type & user permissions
    │
    ▼
[TenantAwareConnectionInterceptor] ── Run SET LOCAL app.current_org_id on DB connection
    │
    ▼
[@RestController Handler] ── Executes business logic inside secure tenant boundary
```
