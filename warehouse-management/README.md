# Warehouse Management

Enterprise-oriented warehouse management backend based on Spring Boot, MyBatis-Plus, Redis and Flyway.

## Highlights

- Token-based authentication with warehouse data scope control
- Safer response models for users, suppliers and customers
- Inbound and outbound order submission flows with stock consistency
- Audited stock adjustment flow with request-level idempotency
- Stock check, loss and overflow workflows with dedicated audit records
- Approval workflow for submitted stock checks, loss reports and overflow reports
- Redis-ready distributed stock lock with local fallback
- Fine-grained permission enforcement via annotated controller permissions
- Flyway-based schema migration and seeded dev/test data
- Operation audit logging and CSV export for compliance scenarios
- Security monitoring endpoints for eBPF event ingestion
- Actuator and Prometheus endpoints for basic observability
- OpenAPI documentation via SpringDoc

## Profiles

- `dev`: local development with H2 and demo seed data
- `mysql`: MySQL datasource override for staging or production-like environments

The default profile is `dev`.

## Required Environment Variables

At minimum, set these in non-dev environments:

```bash
WAREHOUSE_TOKEN_SECRET=replace-with-a-strong-secret
WAREHOUSE_EBPF_INGEST_KEY=replace-with-a-strong-agent-key
```

When using MySQL:

```bash
WAREHOUSE_DB_URL=jdbc:mysql://localhost:3306/warehouse_db?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai
WAREHOUSE_DB_USERNAME=warehouse_app
WAREHOUSE_DB_PASSWORD=replace-with-db-password
```

When enabling distributed stock locks:

```bash
WAREHOUSE_STOCK_LOCK_ENABLED=true
WAREHOUSE_REDIS_HOST=127.0.0.1
WAREHOUSE_REDIS_PORT=6379
WAREHOUSE_REDIS_PASSWORD=
```

## Run Locally

Development mode with H2:

```bash
mvn spring-boot:run
```

MySQL mode:

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

## Testing

```bash
mvn test
```

## Database Migration

Schema changes are managed through Flyway migrations under:

```text
src/main/resources/db/migration
```

Dev-only seed data lives under:

```text
src/main/resources/db/dev
```

## Operational Notes

- `h2-console` is disabled by default and only enabled in `dev`
- Stock, inbound order and outbound order direct CRUD endpoints are intentionally blocked to preserve stock consistency
- Use `/api/inbound/submit` and `/api/outbound/submit` for inventory-affecting operations
- Use `/api/stock/adjustments` for manual stock adjustments instead of editing stock rows directly
- Use `/api/stock/checks`, `/api/stock/losses` and `/api/stock/overflows` for formal inventory workflows
- Use `/api/stock/checks/apply`, `/api/stock/losses/apply`, `/api/stock/overflows/apply` for approval-based inventory workflows
- Use `/api/stock/checks/{id}/approve` and `/api/stock/adjustments/{id}/approve` for approval decisions
- Use `/api/audit/page` and `/api/audit/export` for audit review and export
- Health and metrics endpoints are exposed under `/actuator`
- API docs are exposed under `/swagger-ui.html` and `/api-docs`

## Permission Model

The backend now enforces permission points at the controller boundary.

Examples:

- `stock:read`
- `stock:adjust:write`
- `inbound:submit`
- `outbound:submit`
- `security:monitor:read`

The login response includes the resolved permission list so the frontend can align UI capabilities with backend authorization.

## Approval Notes

- Direct stock adjustment endpoints still exist for privileged immediate operations
- Approval-based requests are stored as `PENDING` and do not change stock until approved
- Approval will fail if the underlying stock quantity has changed since submission, forcing resubmission to avoid stale approvals

## Audit Filters

Audit page and export endpoints support filtering by:

- `action`
- `resource`
- `operatorId`
- `success`
- `requestId`
- `requestUri`
- `fromTime`
- `toTime`

## Built-in Demo Users

- `admin / 123456`
- `operator / 123456`
- `auditor / 123456`

## eBPF Integration

The Java service does not run eBPF probes itself. It receives host-side events from an external Linux agent through:

```text
POST /api/security/ebpf/ingest
```

Agent scripts are located under:

```text
ops/ebpf
```
