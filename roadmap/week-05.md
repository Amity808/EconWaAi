# Week 5 — Registered Supplier Catalogues & Unit Conversions

> **Goal:** Build the registered supplier catalogue management system, implement dynamic local trade unit conversions, support bulk spreadsheet uploads, and enable conversational WhatsApp catalogue syncing.

---

## Monday — Catalog Schema & Domain Models

- [ ] Implement `SupplierCatalogItem` entity and repository in `econwa-catalog`.
- [ ] Implement `UnitConversion` service:
  - Supports standard commodities and local unit multipliers (e.g., 50.0 for 50kg bag, 1.3 for mudu of garri).
  - Supports supplier-level and market-level unit overrides per Section 4.
- [ ] Implement `ProductSynonym` trigram search service using PostgreSQL `pg_trgm` GIN index.
- [ ] Write unit tests verifying that *"derica"*, *"paint rubber"*, and *"carton"* convert accurately to canonical base units.

**Deliverable:** Database and domain models for registered supplier catalogues and unit conversions live with tests.

---

## Tuesday — Supplier Catalog Management REST APIs

- [ ] Implement `GET /v1/supplier/catalog` with pagination, category, and stock status filters.
- [ ] Implement `POST /v1/supplier/catalog` for registering new SKUs:
  - Validates `product_id`, `trade_unit`, `indicative_price_kobo`, `moq`, `pickup_location_id`.
- [ ] Implement `PUT /v1/supplier/catalog/{id}` to update pricing and inventory.
- [ ] Implement `DELETE /v1/supplier/catalog/{id}` (soft-deactivates SKU).
- [ ] Write integration tests for catalogue CRUD endpoints with role-based access checks.

**Deliverable:** Suppliers can manage their registered catalogue via authenticated REST API endpoints.

---

## Wednesday — Bulk Spreadsheet Ingestion (CSV / Excel)

- [ ] Implement `POST /v1/supplier/catalog/bulk-upload`:
  - Parses uploaded `.csv` and `.xlsx` files using Apache POI.
  - Matches raw product text against canonical catalog using trigram fuzzy similarity.
  - Validates trade units, MOQs, and prices.
- [ ] Wrap bulk creation in a single atomic database transaction.
- [ ] Return parsing report detailing successfully imported items and ambiguous rows needing manual review.
- [ ] Write integration test with a 100-row sample wholesale price list.

**Deliverable:** Distributors can bulk-upload hundreds of catalogue items in seconds via Excel.

---

## Thursday — WhatsApp Conversational Catalogue Sync

- [ ] Implement WhatsApp conversational catalog registration:
  - Supplier sends: *"I sell Golden Penny Flour 50kg at 72000 naira, minimum order 5 bags."*
  - AI parses input, maps to canonical flour, and registers SKU in `supplier_catalog_items`.
- [ ] Implement voice/text price and stock updates over WhatsApp:
  - *"Update Mama Gold to 86,000"* ➔ Updates `indicative_price_kobo`.
  - *"Sugar is out of stock"* ➔ Sets `stock_status = 'out_of_stock'`.
- [ ] Implement confirmation reply over WhatsApp: *"✅ Updated Mama Gold Rice 50kg to ₦86,000."*

**Deliverable:** Suppliers can update their live catalogue directly from WhatsApp without touching a computer.

---

## Friday — Price Board Image OCR Pipeline

- [ ] Integrate OCR pipeline (Tesseract / Vision AI) in Python microservice for warehouse price boards.
- [ ] When a supplier sends a photo of their physical warehouse chalkboard or printed invoice on WhatsApp:
  - Crop and extract text lines.
  - Parse commodity names, unit packings, and prices.
  - Return draft catalogue preview to supplier WhatsApp with `[ ✅ Confirm All ]` button.
- [ ] Conduct end-to-end testing with physical market price list photos.

**Deliverable:** Suppliers can snap a photo of their price board to populate their registered catalogue.

---

## Weekend / Catch-Up
- Review sample catalogue data from pilot suppliers in Alaba and Mile 12.
- Prepare RFQ broadcast routing rules for Week 6.
