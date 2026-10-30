# AGENTS.md

Instructions for agents working in `backend/`.

## Project

Spring Boot REST API for GVS Terceirizacoes. The API manages clients, providers, contracts, service orders, and history/audit records.

The implementation already exists. Treat changes as maintenance or extension work unless the user explicitly asks for a rebuild.

## Scope and source of truth

- Work only inside `backend/`.
- Do not modify `desktop/`.
- Do not modify or delete `backend/ts/`. It is the TypeScript reference API, not runtime code.
- Read `backend/plan.md` before making architectural changes.
- If behavior diverges from `plan.md`, update `plan.md` only when the user approves the change.

## Tech stack

- Java 25
- Spring Boot `4.2.0-SNAPSHOT`
- Spring Web MVC
- Spring Data JPA / Hibernate
- MySQL at runtime
- H2 for tests
- Bean Validation
- JJWT `0.12.6`
- BCrypt via `spring-security-crypto`
- Springdoc OpenAPI `3.2.0`
- Lombok
- JUnit 5 + MockMvc

Base package: `com.h_group.gvs_terceirizacoes`

## Commands

Run from `backend/`:

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw -q -DskipTests package
```

Use `./mvnw test` as the primary verification command.

## Environment

The application expects these environment variables:

```text
PORT
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

`application.properties` uses these placeholders directly. Spring Boot does not automatically load `.env`, so export the variables or configure them in the IDE run configuration.

Never commit `.env` or real secrets. Use `.env.example` as the template. Note that `.env` is currently not ignored by `.gitignore`; do not stage it, and add it to `.gitignore` only as an approved change.

The JWT secret must be at least 32 bytes for HS256. Shorter secrets are hashed with SHA-256 by `JwtService`.

## Architecture

Keep the existing layered structure:

```text
controller -> service -> repository -> entity
```

Package conventions:

- `config`: CORS, JWT, filter registration, password encoder, OpenAPI.
- `controller`: HTTP endpoints only.
- `dto/request`: validated request records.
- `dto/response`: response records.
- `entity`: JPA entities and enums.
- `exception`: `ApiException` and global exception handling.
- `repository`: Spring Data JPA interfaces.
- `service`: business rules and transactions.

Rules:

- Controllers must not expose entities directly.
- Controllers accept request DTOs and return response DTOs.
- Services own business logic and map entities to response DTOs.
- Repositories contain persistence queries only.
- Use constructor injection. Do not use field injection.
- Use `@Transactional` on write service methods and `@Transactional(readOnly = true)` on read methods.
- Keep UUID primary keys with `GenerationType.UUID`.
- Use `@EntityGraph` on relationship-heavy repository methods to avoid lazy-loading errors.

## API contract

URL paths remain Portuguese. JSON fields are English.

| Resource | Base path |
|---|---|
| Authentication | `/api/auth` |
| Clients | `/api/clientes` |
| Providers | `/api/prestadores` |
| Contracts | `/api/contratos` |
| Service orders | `/api/ordens-servico` |
| History | `/api/historico` |

Standard CRUD endpoints:

```text
GET    /api/<resource>
GET    /api/<resource>/{id}
POST   /api/<resource>
PUT    /api/<resource>/{id}
DELETE /api/<resource>/{id}
```

History supports only `GET` and `POST`.

Public endpoints:

```text
GET /
POST /api/auth/register
POST /api/auth/login
GET /api-docs
GET /swagger-ui.html
```

All resource routes require:

```text
Authorization: Bearer <jwt>
```

Response status conventions:

- `201` on create
- `204` on delete
- `400` on validation errors
- `401` on missing/invalid token or bad login
- `404` on missing resources
- `409` on duplicates or data integrity conflicts
- `500` on unexpected errors

IDs are UUIDs exposed as `id`. Do not introduce Mongo-style `_id`.

## JSON conventions

- Use English field names.
- Use camelCase for JSON properties.
- Use lowercase English enum values:
  - Client: `active`, `inactive`
  - Contract: `active`, `closed`
  - Service order: `pending`, `in_progress`, `completed`, `canceled`
- Enum serialization/deserialization uses `@JsonValue` and `@JsonCreator`.
- Dates:
  - `LocalDate` for contract and service-order dates
  - `Instant` for timestamps and history date
- Money uses `BigDecimal`.

Contract responses include both IDs and nested objects:

```json
{
  "clientId": "uuid",
  "client": { "...": "..." },
  "providerId": "uuid",
  "provider": { "...": "..." }
}
```

Service-order responses include both `contractId` and nested `contract`.

## Validation

- Request DTOs use Jakarta validation annotations.
- Controllers use `@Valid @RequestBody`.
- Keep validation messages in English.
- Do not add a second validation mechanism; Bean Validation is the standard here.

## Error handling

Use `ApiException` for expected domain errors and `GlobalExceptionHandler` for HTTP mapping.

Error shape:

```json
{
  "status": "error",
  "error": "Client not found"
}
```

Validation error shape:

```json
{
  "status": "error",
  "error": "Validation failed",
  "details": [
    { "field": "cnpj", "message": "must not be blank" }
  ]
}
```

Do not add ad-hoc `try/catch` blocks in controllers for errors already handled globally. Log unexpected exceptions in the global handler, not in controllers.

## Security

- Do not add the full Spring Security starter unless the user explicitly requests it.
- Authentication is implemented with:
  - `JwtService`
  - `JwtAuthenticationFilter`
  - `FilterConfig`
  - `PasswordConfig`
- The JWT filter protects only these prefixes:
  - `/api/clientes`
  - `/api/prestadores`
  - `/api/contratos`
  - `/api/ordens-servico`
  - `/api/historico`
- Keep authentication, Swagger, and root endpoints public.
- Passwords must always be stored using `BCryptPasswordEncoder`.
- Never log passwords, JWTs, database credentials, or `.env` contents.

## Swagger/OpenAPI

- OpenAPI JSON: `/api-docs`
- Swagger UI: `/swagger-ui.html`
- Protected controllers use `@SecurityRequirement(name = "bearerAuth")`.
- Keep controller tags stable: `Authentication`, `Clients`, `Providers`, `Contracts`, `Service Orders`, `History`.
- Springdoc is pinned to `3.2.0`. Verify compatibility before changing the Spring Boot parent version.

## Testing

- Use `@SpringBootTest`, `@AutoConfigureMockMvc`, and the H2 test configuration.
- Test files:
  - `AuthControllerIntegrationTest`
  - `ClientControllerIntegrationTest`
  - `ResourceFlowIntegrationTest`
  - `GvsTerceirizacoesApplicationTests`
- Use unique emails, CNPJs, and tax IDs per test to avoid collisions.
- When adding a resource or endpoint, include at least one happy-path test and one validation/authorization test.
- Always run `./mvnw test` before reporting completion.

## Known constraints and gotchas

- `desktop/` currently expects Portuguese JSON keys and `_id`, while this API exposes English fields and `id`. Do not "fix" the backend contract unless the user explicitly asks; changing it is a product decision.
- `backend/ts/` uses Mongoose and is reference-only. Do not copy Mongo-specific behavior into JPA code.
- MySQL must be reachable at runtime. Tests use H2 and do not require MySQL.
- `History.details` uses Hibernate JSON mapping. If H2/MySQL compatibility becomes a problem, use a text column with manual Jackson serialization.
- The project uses a Spring Boot snapshot. Avoid dependency upgrades unless necessary, and run the full test suite afterward.
- The git worktree may contain unrelated modifications outside `backend/`. Do not stage or commit them.

## Definition of done

Before finishing a task:

1. The change stays within `backend/`.
2. English identifiers and JSON fields are preserved.
3. Portuguese URL paths are preserved.
4. Auth behavior remains correct.
5. Swagger still works.
6. `./mvnw test` passes.
7. No secrets or `.env` values were added to source or documentation.
8. `plan.md` is updated only if the approved design changed.
