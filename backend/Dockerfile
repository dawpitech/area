FROM golang:alpine AS base
WORKDIR /app

FROM base AS deps
COPY go.mod go.sum ./
RUN go mod download

FROM base AS builder
COPY --from=deps /go/pkg /go/pkg
COPY . .
RUN go build -o api .
RUN go build -o migrate_bin migrate/migrate.go

FROM alpine:latest AS release
WORKDIR /app
COPY --from=builder /app/api .
COPY --from=builder /app/migrate_bin .
EXPOSE 24680
CMD [ "./api" ]
