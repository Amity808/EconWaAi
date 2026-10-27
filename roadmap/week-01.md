# Week 1 — Foundation: Java 21 Backend & External Partner Onboarding

> **Goal:** Set up the Java 21 / Spring Boot 3 multi-module Maven workspace, PostgreSQL 16 schema, Docker development environment, and initiate external business verification applications with Meta and the Partner MFB.

---

## Monday — Project Initialization & Maven Workspace Setup

- [ ] Initialize root Maven project `pom.xml` with Java 21 LTS compiler configuration.
- [ ] Create parent module structure:
  - `econwa-common`
  - `econwa-domain`
  - `econwa-infrastructure`
  - `econwa-api-app`
- [ ] Configure Spring Boot 3.3.x dependency management in parent POM.
- [ ] Enable Java 21 Virtual Threads (`spring.threads.virtual.enabled=true`).
- [ ] Set up `docker-compose.yml` (PostgreSQL 16, Redis 7, MinIO / LocalStack for S3 testing).
- [ ] Create `.env.example` with database, Redis, JWT, and partner credentials.
- [ ] Verify `mvn clean compile` and `docker compose up` run end-to-end.
- [ ] **Section 20 Realism Task**: Submit Meta WhatsApp Business verification and Partner MFB developer portal access applications.

**Deliverable:** Maven multi-module project builds cleanly; local Docker containers spin up without errors.

---

## Tuesday — Common Primitives, Money Type & Error Envelope

- [ ] Implement `econwa-common/model/Kobo.java` — immutable Record wrapping `long amount`, with arithmetic helpers and currency formatting (`₦`).
- [ ] Implement `econwa-common/exception/AppException.java` with standard HTTP error codes (`VALIDATION_ERROR`, `QUOTE_EXPIRED`, `UNAUTHORIZED`, `CONFLICT`).
- [ ] Implement `@RestControllerAdvice` in `econwa-api-app` for standard RFC 7807 / JSON error responses.
- [ ] Implement `econwa-common/context/OrgContext.java` — context holder with `orgId`, `orgType`, `userId`, `role`.
- [ ] Configure Logback with structured JSON logging and correlation IDs (`X-Correlation-ID`).
- [ ] Add unit tests for `Kobo` rounding and math validations.

**Deliverable:** Base types compile, custom exceptions return unified JSON errors, structured logging active.

---

## Wednesday — Database Migrations & Multi-Party Schema

- [ ] Configure HikariCP connection pool and Flyway in `econwa-infrastructure`.
- [ ] Write migration `V1__init_extensions_and_enums.sql` (`uuid-ossp`, `pg_trgm`, `org_type_enum`, `kyc_status_enum`, `user_role_enum`).
- [ ] Write migration `V2__create_organizations_and_locations.sql` (implements Section 20.2 landmark fields).
- [ ] Write migration `V3__create_users_and_consents.sql` (NDPA 2023 compliant consent table).
- [ ] Write migration `V4__create_catalog_and_unit_conversions.sql` (canonical products, local trade units, brand synonyms).
- [ ] Write migration `V5__create_supplier_catalog_items.sql` (registered supplier inventory table).
- [ ] Execute `mvn flyway:migrate` against local PostgreSQL 16 container.

**Deliverable:** PostgreSQL 16 database migrated with initial 6 tables and indexes.

---

## Thursday — Repositories & Health Check Endpoints

- [ ] Implement Spring Data JPA repositories:
  - `OrganizationRepository`
  - `OrganizationLocationRepository`
  - `UserRepository`
  - `ConsentRepository`
- [ ] Implement Spring Actuator health indicators (`/actuator/health` checking DB and Redis connectivity).
- [ ] Configure Redis connection via Lettuce and test session caching.
- [ ] Write Spring Boot slice tests (`@DataJpaTest`) verifying repository CRUD and query methods.
- [ ] **Section 20 Realism Task**: Follow up on Partner MFB sandbox credentials and Identity Verification Provider (Prembly) API keys.

**Deliverable:** Database and Redis health checks reporting `UP`; automated JPA slice tests passing.

---

## Friday — Security Foundation & CI Pipeline

- [ ] Implement Spring Security filter chain: stateless session management, CORS configuration for Next.js web portal.
- [ ] Configure Swagger / OpenAPI 3.0 via `springdoc-openapi-starter-webmvc-ui` at `/swagger-ui.html`.
- [ ] Create GitHub Actions workflow (`.github/workflows/ci.yml`) for `mvn clean verify` and Docker image build.
- [ ] Implement `Dockerfile` using multi-stage build with `eclipse-temurin:21-jre-alpine`.
- [ ] Run full test suite in CI.

**Deliverable:** Secure API skeleton operational with live Swagger UI and automated GitHub CI.

---

## Weekend / Catch-Up
- Verify sandbox webhook endpoints using ngrok.
- Review Section 20 discovery interview findings with field pilot partners.
