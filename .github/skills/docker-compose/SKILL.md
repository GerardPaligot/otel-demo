---
name: docker-compose
description: "Manage the local observability stack via Docker Compose. Use when: starting, stopping, or restarting services (Grafana, Tempo, Loki, OTel Collector), troubleshooting connectivity, checking service logs, or verifying the stack is healthy."
---

# Docker Compose — Observability Stack

## When to Use

- Starting or restarting the observability stack
- Troubleshooting service connectivity or missing data
- Checking logs of any service
- Verifying the stack is healthy after configuration changes

## Stack Overview

| Service | Image | Ports | Config File |
|---------|-------|-------|-------------|
| `otel-collector` | `otel/opentelemetry-collector-contrib:0.123.0` | 4317 (gRPC), 4318 (HTTP), 8888 (metrics) | `observability/otel-collector/config.yaml` |
| `tempo` | `grafana/tempo:2.7.2` | 3200 | `observability/tempo/tempo.yaml` |
| `loki` | `grafana/loki:3.5.0` | 3100 | `observability/loki/loki.yaml` |
| `grafana` | `grafana/grafana:11.6.0` | 3000 | `observability/grafana/datasources/datasources.yaml` |

Config file: `docker-compose.yml` (project root)

## Data Flow

```
Android App (OTLP/HTTP :4318)
  → OTel Collector
    → Tempo (:4317 gRPC) for traces
    → Loki (:3100 OTLP/HTTP) for logs
  → Grafana reads from Tempo + Loki
```

## Common Commands

### Start the full stack
```bash
docker compose up -d
```

### Stop the full stack
```bash
docker compose down
```

### Restart a single service (e.g., after dashboard/config changes)
```bash
docker compose restart grafana
docker compose restart otel-collector
```

### View logs
```bash
docker compose logs -f otel-collector    # follow collector logs
docker compose logs tempo --tail=50      # last 50 Tempo lines
docker compose logs loki --tail=50       # last 50 Loki lines
```

### Check service health
```bash
docker compose ps                                    # all services status
curl -s http://localhost:3200/ready                   # Tempo readiness
curl -s http://localhost:3100/ready                   # Loki readiness
curl -s http://localhost:8888/metrics | head -5       # Collector self-metrics
```

## When to Restart What

| Change | Command |
|--------|---------|
| Dashboard JSON edited | `docker compose restart grafana` |
| Datasource YAML edited | `docker compose restart grafana` |
| Collector config edited | `docker compose restart otel-collector` |
| Tempo config edited | `docker compose restart tempo` |
| Loki config edited | `docker compose restart loki` |
| All configs changed | `docker compose down && docker compose up -d` |

## Android App Connection

- **Emulator**: App sends to `http://10.0.2.2:4318/v1/traces` (Android emulator alias for host machine's localhost)
- **Physical device**: Update the endpoint in `MainApplication.kt` to your LAN IP (e.g., `http://192.168.1.X:4318`)

## Troubleshooting

### No traces in Grafana
1. Check Collector is receiving data: `docker compose logs -f otel-collector` — look for span export logs
2. Check Tempo is up: `curl http://localhost:3200/ready`
3. Verify the app endpoint points to the Collector

### No logs in Grafana
1. Check Collector logs pipeline: `docker compose logs otel-collector | grep -i log`
2. Check Loki is up: `curl http://localhost:3100/ready`
3. Verify Loki receives OTLP: `curl -s "http://localhost:3100/loki/api/v1/query" --data-urlencode 'query={service_name="com.paligot.otel-demo"}' | python3 -m json.tool | head -10`

### Grafana shows "No data"
1. Check the time range (default: last 1h) — the app must have been running recently
2. Verify datasource connectivity: Grafana → Connections → Data sources → Test
3. Restart Grafana: `docker compose restart grafana`

## Volumes

Three named volumes persist data across restarts:
- `tempo-data` — trace blocks and WAL
- `loki-data` — log chunks and index
- `grafana-data` — Grafana state (user prefs, annotations)

To fully reset: `docker compose down -v` (destroys all stored data)
