#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"

cd "$BACKEND_DIR"
export GOCACHE="${GOCACHE:-/tmp/dailycanvas-go-build}"
go tool github.com/oapi-codegen/oapi-codegen/v2/cmd/oapi-codegen \
  --config cmd/openapi/server.cfg.yaml \
  "$ROOT_DIR/api/openapi.yaml"
