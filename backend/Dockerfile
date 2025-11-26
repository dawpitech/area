FROM golang:1.24.9-alpine AS base
WORKDIR /app

FROM base AS deps
COPY go.mod go.sum ./
RUN go mod download

FROM base AS builder
COPY --from=deps /go/pkg /go/pkg
COPY . .
RUN go build -o api .

FROM alpine:latest AS release
WORKDIR /app
COPY --from=builder /app/api .
EXPOSE 24680
CMD [ "./api" ]
