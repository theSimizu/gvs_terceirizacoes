# AGENTS.md — GVS Terceirizações Desktop

## Overview

JavaFX desktop client backed by Spring Boot (non-web), talking to the GVS REST API
through Spring Cloud OpenFeign. Login/registration only at present.

- Java 25, Spring Boot 4.2.0-SNAPSHOT, JavaFX 25.0.5, Maven Wrapper.
- Base package: `com.h_group.gvs_terceirizacoes_desktop`
- API base URL: `gvs.api.base-url` (default `http://localhost:8080/api`)
- The app is not a web server: `spring.main.web-application-type=none`.

## Commands

Run from `desktop/`:

- Compile: `./mvnw -q compile`
- Test: `./mvnw test` (only a Spring context-load test currently)
- Run app: `./mvnw javafx:run`
- Full Feign/HTTP debug logging: activate the `debug-http` profile, e.g.
  `./mvnw javafx:run -Dspring-boot.run.profiles=debug-http` or pass
  `--spring.profiles.active=debug-http`.
- Manual login/register testing requires the backend running at the configured base URL.

## Layout

- `api/` — Feign clients (`AuthClient`), auth interceptor, error decoder, exceptions.
- `config/` — `FeignConfig`, `ApiProperties`.
- `dto/` — Java records for request/response payloads.
- `service/` — orchestration and error translation (`AuthService`).
- `session/` — in-memory auth session/token.
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

- `dto/LoginResponse` and `dto/UserDTO` still map response keys `usuario` / `nome`,
  while the backend returns `user` / `name`. Reconcile these when touching auth responses.
