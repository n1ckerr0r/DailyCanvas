# DailyArt

OpenAPI specification: `api/openapi.yaml`

The project has three application areas:

- `backend/` - API, Postgres, migrations, and local artwork images.
- `frontend/` - desktop browser frontend.
- `android/` - native Android client built with Kotlin and Jetpack Compose.

Developer documentation is split by area:

- [backend/README.md](/home/n1ckerr0r/maindir/projects/daily-canvas/backend/README.md)
- [frontend/README.md](/home/n1ckerr0r/maindir/projects/daily-canvas/frontend/README.md)
- [android/README.md](/home/n1ckerr0r/maindir/projects/daily-canvas/android/README.md)

## Running The Project

1. Start the backend and Postgres:

```bash
cd backend
docker compose up --build
```

2. After startup, these endpoints are available:

- desktop frontend: `http://localhost:37117/`
- API: `http://localhost:37117/api/v1`
- Postgres: `localhost:37432`

3. For Android:

- open `android/` in Android Studio
- create `android/local.properties` from `android/local.properties.example`
- verify `dailycanvas.api.baseUrl`
- run the `app` configuration

## Working With The Project

- The API contract is changed in `api/openapi.yaml`.
- After changing OpenAPI, regenerate the backend transport layer:

```bash
./backend/cmd/openapi/generate.sh
```

- Artwork seed data lives in `backend/migrations/001_init.sql`.
- Artwork image files live in `backend/storage/images/`.
- The desktop frontend reads runtime config from `frontend/config.js`.
- Android reads the backend URL from `local.properties` or the Gradle property `dailycanvas.api.baseUrl`.

## Local Commands

Run the backend without Docker:

```bash
cd backend
GOCACHE=/tmp/dailycanvas-go-build GOSUMDB=off go run ./cmd/api
```

Run backend tests:

```bash
cd backend
GOCACHE=/tmp/dailycanvas-go-build GOSUMDB=off go test ./...
```
