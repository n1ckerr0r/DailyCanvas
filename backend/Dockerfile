FROM golang:1.26.1-alpine AS builder

WORKDIR /app

COPY backend/go.mod backend/go.sum* ./backend/
WORKDIR /app/backend
RUN go mod download

WORKDIR /app
COPY backend ./backend
RUN cd backend && CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o /tmp/dailycanvas-api ./cmd/api

FROM alpine:3.22

WORKDIR /app

COPY --from=builder /tmp/dailycanvas-api /usr/local/bin/dailycanvas-api
COPY backend /app/backend
COPY frontend /app/frontend

WORKDIR /app/backend
EXPOSE 37117

CMD ["dailycanvas-api"]
