# Backend

This directory contains the Go backend in an `entity-first` hexagonal layout.

## File Responsibilities

- `cmd/api/main.go` - backend entry point, dependency wiring, and HTTP server startup.
- `cmd/openapi/` - config and script for generating the transport layer from `api/openapi.yaml`.
- `internal/artwork/` - domain logic for artworks, calendar data, tags, and artwork of the day.
- `internal/favorite/` - favorite artwork domain logic.
- `internal/settings/` - user settings.
- `internal/user/` - user context.
- `internal/auth/` - token issuing and auth responses without hardcoded token strings.
- `internal/adapters/http/` - HTTP adapter, DTO mapping, and routing.
- `internal/adapters/postgres/` - Postgres repositories.
- `internal/platform/config/` - environment config loading.
- `internal/platform/postgres/` - database connection and SQL migration runner.
- `migrations/` - database schema and seed data.
- `storage/images/` - local artwork image storage.
- `docker-compose.yml` - local backend + Postgres runtime.
- `Dockerfile` - backend container image.

## Configuration

Main environment variables:

- `APP_PORT` - API port.
- `WEB_DIR` - path to the desktop frontend that the backend can serve as the root site.
- `IMAGES_DIR` - path to local images.
- `MIGRATIONS_DIR` - path to SQL migrations.
- `PUBLIC_BASE_URL` - public base URL of the application.
- `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_SSLMODE` - Postgres settings.
- `AUTH_TOKEN_TYPE`, `AUTH_ACCESS_TOKEN_TTL_SECONDS`, `AUTH_REFRESH_TOKEN_TTL_SECONDS` - token settings.

## Main Commands

Generate the transport layer:

```bash
./cmd/openapi/generate.sh
```

Run locally without Docker:

```bash
GOCACHE=/tmp/dailycanvas-go-build GOSUMDB=off go run ./cmd/api
```

Run tests:

```bash
GOCACHE=/tmp/dailycanvas-go-build GOSUMDB=off go test ./...
```

Start the backend and Postgres in Docker:

```bash
docker compose up --build
```
