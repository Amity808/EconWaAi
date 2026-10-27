# 05 — API Design

## 1. API Conventions

| Dimension | Specification |
|:---|:---|
| **Base URL** | `https://api.econwa.ai/v1` |
| **Data Format** | JSON (UTF-8) |
| **Authentication** | Bearer JWT via `Authorization: Bearer <token>` |
| **Tenant Scope** | Inferred from JWT claims (`org_id`, `org_type`, `role`) |
| **Monetary Units** | **Always `BIGINT` in kobo** (₦1 = 100 kobo). Never decimals. |
| **Date / Time** | ISO 8601 UTC (`2026-10-23T14:30:00Z`) |
| **Idempotency** | Supported via `Idempotency-Key: <UUID>` header for all mutations |

### 1.1 Standard Error Envelope
```json
{
  "error": {
    "code": "QUOTE_EXPIRED",
    "message": "The quotation has expired due to price validity window timeout.",
    "details": [
      {
        "field": "valid_until",
        "message": "Quote validity ended at 2026-10-23T12:00:00Z. Please request a re-quote."
      }
    ]
  }
}
```

---

## 2. RFQ, Speech & AI Parsing Endpoints

### 2.1 Ingest Voice Note RFQ
`POST /v1/rfq/voice`
- **Auth**: Buyer JWT or Internal WhatsApp Ingestion Service
- **Content-Type**: `multipart/form-data`
- **Form Fields**: `audio_file` (audio/ogg, audio/m4a, audio/wav), `destination_location_id` (UUID)

**Response `200 OK`**:
```json
{
  "rfq_id": "018f4b23-901a-7b3e-91c2-552d7e012345",
  "transcription": "I want twenty bags of Mama Gold fifty kg and ten cartons of Dangote sugar to Trade Fair Peace Plaza",
  "ai_confidence_score": 0.94,
  "extracted_items": [
    {
      "product_id": "018f4a11-1234-7b3e-91c2-552d7e000001",
      "canonical_name": "Mama Gold Parboiled Rice",
      "quantity": 20,
      "trade_unit": "50kg_bag",
      "confidence": 0.96
    },
    {
      "product_id": "018f4a11-1234-7b3e-91c2-552d7e000002",
      "canonical_name": "Dangote Refined Sugar",
      "quantity": 10,
      "trade_unit": "carton",
      "confidence": 0.92
    }
  ],
  "plain_language_confirmation": "You requested: 20 x 50kg bags of Mama Gold Rice and 10 cartons of Dangote Sugar to Trade Fair Peace Plaza.",
  "status": "pending_buyer_confirmation"
}
```

---

## 3. Quotations & Step 9 Reconfirmation

### 3.1 Submit Quotation (Supplier)
`POST /v1/rfq/{rfq_id}/quotes`
- **Auth**: Supplier JWT

**Request Payload**:
```json
{
  "pickup_location_id": "018f4a12-5678-7b3e-91c2-552d7e000003",
  "valid_until": "2026-10-24T18:00:00Z",
  "items": [
    {
      "product_id": "018f4a11-1234-7b3e-91c2-552d7e000001",
      "unit_price_kobo": 8500000,
      "quantity": 20,
      "trade_unit": "50kg_bag"
    }
  ]
}
```

### 3.2 Step 9 Supplier Order Reconfirmation
`POST /v1/orders/{order_id}/step-9-reconfirm`
- **Auth**: Supplier JWT
- **Rule**: If commodity market prices surged, supplier updates price; status changes to `pending_buyer_approval`.

```json
{
  "action": "reconfirm_with_price_adjustment",
  "adjusted_unit_price_kobo": 8600000,
  "reason": "Sudden warehouse gate fee increase in Mile 12"
}
```

---

## 4. Payments & Partner MFB Endpoints

### 4.1 Generate Order Virtual Account
`POST /v1/orders/{order_id}/virtual-account`
- **Auth**: Buyer JWT

**Response `201 Created`**:
```json
{
  "order_id": "018f4b55-7788-7b3e-91c2-552d7e012345",
  "bank_name": "EconWa Partner MFB",
  "account_number": "9928374610",
  "account_name": "EconWa / Chinedu Wholesale Store",
  "total_amount_kobo": 172000000,
  "currency": "NGN",
  "instructions": "Transfer exact amount to the dedicated NUBAN. Settlement verifies automatically via MFB webhook within 60 seconds."
}
```

### 4.2 Partner MFB Webhook Ingestion
`POST /v1/webhooks/mfb-settlement`
- **Headers**: `X-MFB-Signature: sha256=...` (HMAC verification)
- **Constraint**: Orders mark as `paid_escrow` strictly through this handler.

**Request Payload**:
```json
{
  "event": "transfer.settled",
  "session_id": "99912837461928374",
  "account_number": "9928374610",
  "amount_kobo": 172000000,
  "reference": "MFB-TX-20261023-8841",
  "sender_name": "CHINEDU STORES LIMITED",
  "settled_at": "2026-10-23T14:45:10Z"
}
```

---

## 5. ProcureAI Credit & BNPL Underwriting

### 5.1 Query Trade Score & Available Credit
`GET /v1/credit/my-status`
- **Auth**: Buyer JWT

**Response `200 OK`**:
```json
{
  "buyer_org_id": "018f4a32-721a-7b3e-91c2-552d7e067890",
  "trade_score": 780,
  "credit_tier": "silver",
  "total_credit_limit_kobo": 500000000,
  "available_credit_kobo": 328000000,
  "repayment_period_days": 14,
  "interest_rate_monthly_bps": 250,
  "is_frozen": false,
  "score_factors": {
    "completed_orders_positive": "+140 pts",
    "ontime_repayments": "+80 pts",
    "disputes_deduction": "0 pts"
  }
}
```

### 5.2 Execute Credit Drawdown (Restocking)
`POST /v1/credit/drawdown`
- **Auth**: Buyer JWT + OTP Verification

```json
{
  "order_id": "018f4b55-7788-7b3e-91c2-552d7e012345",
  "drawdown_amount_kobo": 172000000,
  "otp_code": "849201",
  "agreed_due_date": "2026-11-06T23:59:59Z"
}
```
*Note: The platform transfers funds directly to the supplier's MFB virtual sub-account.*

---

## 6. Logistics & Ultra-Lightweight Driver API

### 6.1 Available Jobs (Broadcast Pool)
`GET /v1/logistics/jobs/available`
- **Auth**: Transporter JWT
- Returns list with masked exact addresses (pickup zone, delivery market, vehicle class, weight).

### 6.2 Driver Full Job Details (Assigned Driver)
`GET /v1/driver/jobs/{job_id}`
- **Auth**: Driver JWT
- Returns complete landmark directions, market stall coordinates, recipient phone, and map pin.

### 6.3 Driver Offline-Queued Events Ingestion
`POST /v1/driver/jobs/{job_id}/events`
- **Auth**: Driver JWT
- Accepts array of cached events collected during network blackout.

```json
{
  "events": [
    {
      "event_type": "driver_called_recipient",
      "device_captured_at": "2026-10-23T15:10:00Z",
      "latitude": 6.4531,
      "longitude": 3.3958
    },
    {
      "event_type": "arrived_destination",
      "device_captured_at": "2026-10-23T15:22:15Z",
      "latitude": 6.4540,
      "longitude": 3.3962
    }
  ]
}
```

### 6.4 Handover Completion & POD Submission
`POST /v1/driver/jobs/{job_id}/complete`
- **Auth**: Driver JWT
- **Payload**:
```json
{
  "handover_otp": "726194",
  "delivery_photo_url": "https://cdn.econwa.ai/pod/018f4b22.jpg",
  "notes": "Delivered to shop assistant Mr. Segun at Shop 44"
}
```

---

## 7. Registered Supplier Catalogue Management Endpoints

### 7.1 List Supplier's Registered Catalogue Items
`GET /v1/supplier/catalog`
- **Auth**: Supplier JWT
- **Query Params**: `?category=grains&stock_status=in_stock`

### 7.2 Register / Add New Catalogue Item
`POST /v1/supplier/catalog`
- **Auth**: Supplier JWT

**Request Payload**:
```json
{
  "product_id": "018f4a11-1234-7b3e-91c2-552d7e000001",
  "custom_product_name": "Mama Gold Parboiled Rice (Premium Grade)",
  "trade_unit": "50kg_bag",
  "moq": 10,
  "indicative_price_kobo": 8500000,
  "stock_status": "in_stock",
  "available_quantity": 450,
  "pickup_location_id": "018f4a12-5678-7b3e-91c2-552d7e000003"
}
```

### 7.3 Bulk Catalogue Upload (CSV / Excel)
`POST /v1/supplier/catalog/bulk-upload`
- **Auth**: Supplier JWT
- **Content-Type**: `multipart/form-data`
- Parses spreadsheet, maps to canonical products via trigram matching, and registers catalogue items in a single atomic transaction.

### 7.4 Quick Stock / Price Sync (Web or WhatsApp Bot)
`PATCH /v1/supplier/catalog/{item_id}`
- **Auth**: Supplier JWT or Verified Supplier WhatsApp Phone Webhook
- Updates `indicative_price_kobo`, `stock_status`, or `available_quantity`.
