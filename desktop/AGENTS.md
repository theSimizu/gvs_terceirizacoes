# AGENTS.md — GVS Terceirizações Desktop

## Overview

JavaFX desktop client backed by Spring Boot (non-web), talking to the GVS REST API
through Spring Cloud OpenFeign. Login, registration, and a read-only dashboard are
implemented; the remaining navigation sections are placeholders.

- Java 25, Spring Boot 4.2.0-SNAPSHOT, JavaFX 25.0.5, Maven Wrapper.
- Base package: `com.h_group.gvs_terceirizacoes_desktop`
- API base URL: `gvs.api.base-url` (default `http://localhost:8080/api`)
- The app is not a web server: `spring.main.web-application-type=none`.
- The auth token is persisted at `gvs.session.file` (default
  `~/.gvs-terceirizacoes/session.json`, mode 0600) and restored on startup;
  expired tokens are discarded.

## Commands

Run from `desktop/`:

- Compile: `./mvnw -q compile`
- Test: `./mvnw test` (Spring context-load plus session/token-store unit tests)
- Run app: `./mvnw javafx:run`
- Full Feign/HTTP debug logging: activate the `debug-http` profile, e.g.
  `./mvnw javafx:run -Dspring-boot.run.profiles=debug-http` or pass
  `--spring.profiles.active=debug-http`.
- Manual login/register testing requires the backend running at the configured base URL.

## Layout

- `api/` — Feign clients (`AuthClient` plus the dashboard clients), auth interceptor,
  error decoder, exceptions.
- `config/` — `FeignConfig`, `ApiProperties`, `SessionProperties`.
- `dto/` — Java records for request/response payloads.
- `service/` — orchestration, error translation, and dashboard aggregation
  (`AuthService`, `DashboardService`).
- `session/` — in-memory auth session plus the persisted token store
  (`~/.gvs-terceirizacoes/session.json`, mode 0600).
- `ui/` — JavaFX views and `Navigator`.
- `validation/` — input validation.
- `html/` — legacy reference admin frontend; ignored by Git and not part of the Maven build.

## Conventions

- Class, field, method, and variable names in English.
- JSON request/response keys in English (e.g. `password`, `name`, `email`), set via
  `@JsonProperty` on record components.
- URL paths stay Portuguese (`/auth/login`, `/auth/register`) for API compatibility.
- User-facing UI text is Portuguese; keep it that way.
- DTOs are Java records. Use constructor injection; no field injection.
- Use tabs for indentation, matching existing Java sources.
- Do not add comments unless necessary to explain non-obvious behavior.
- Route all HTTP calls through Feign clients in `api/`; do not use `HttpClient` directly.
- Attach auth via `AuthTokenInterceptor`; handle errors via `ApiErrorDecoder` / `ApiException`.

## Testing

- Add tests under `src/test/java/...` and run `./mvnw test`.
- No formatter/linter is configured; compilation and tests are the checks.
- Integration behavior depends on the backend; keep DTO keys and paths in sync with it.

## Known inconsistencies

- `dto/RegisterResponse` expects `token` / `usuario` / `message`, while the backend
  `POST /auth/register` returns a bare `UserResponse` (`id`, `name`, `email`). The
  desktop treats a missing token as "registration succeeded, sign in".
