# Observability Stack – Setup Guide

This guide walks through running the full observability stack locally and verifying that
OpenTelemetry signals emitted by the Android app reach Grafana.

## Architecture overview

```
Android app
  └─ OtlpHttpSpanExporter  →  POST http://10.0.2.2:4318/v1/traces
                                       │
                              ┌────────▼────────┐
                              │  OTel Collector  │  :4318 (HTTP) / :4317 (gRPC)
                              └────────┬────────┘
                                       │ OTLP gRPC
                              ┌────────▼────────┐
                              │   Grafana Tempo  │  :3200
                              └────────┬────────┘
                                       │ data source
                              ┌────────▼────────┐
                              │     Grafana      │  :3000
                              └─────────────────┘
```

## Prerequisites

| Tool | Minimum version | Install |
|------|----------------|---------|
| Docker Desktop | 4.x | https://www.docker.com/products/docker-desktop |
| Docker Compose | v2 (bundled with Docker Desktop) | – |

Verify both are available:

```bash
docker --version        # Docker version 27.x.x
docker compose version  # Docker Compose version v2.x.x
```

---

## Step 1 – Start the stack

From the **root of the repository** (where `docker-compose.yml` lives):

```bash
docker compose up -d
```

This starts three containers:

| Container | Image | Purpose |
|-----------|-------|---------|
| `otel-collector` | `otel/opentelemetry-collector-contrib:0.123.0` | Receives OTLP spans, forwards to Tempo |
| `tempo` | `grafana/tempo:2.7.2` | Stores and serves traces |
| `grafana` | `grafana/grafana:11.6.0` | Dashboards and trace explorer |

Check that all three are running:

```bash
docker compose ps
```

Expected output — all services should show `running`:

```
NAME              STATUS
otel-collector    running
tempo             running
grafana           running
```

---

## Step 2 – Verify the Collector is ready

```bash
curl -s http://localhost:8888/metrics | grep otelcol_receiver_accepted_spans
```

At this point the counter will be `0` (no spans yet). The absence of an error confirms
the Collector is up and its health endpoint is reachable.

---

## Step 3 – Open Grafana

Open **http://localhost:3000** in your browser.

No login is required — anonymous access with Admin rights is enabled by default.

Navigate to **Connections → Data sources** and confirm that **Tempo** appears with a
green "Data source connected" badge. The data source is provisioned automatically from
[`observability/grafana/datasources/datasources.yaml`](grafana/datasources/datasources.yaml).

---

## Step 4 – Run the Android app and send spans

### Emulator (default)

The app is pre-configured to send spans to `http://10.0.2.2:4318/v1/traces`.
`10.0.2.2` is the special alias that the Android emulator uses to reach `localhost`
on the host machine. No extra configuration is needed.

1. Start an AVD from Android Studio.
2. Run the app (`▶` or `Shift+F10`).
3. Interact with any screen (home loads, schedule date changes, speaker searches, etc.).

### Physical device

`10.0.2.2` does not work on a physical device. You need to point the exporter at your
machine's LAN IP address.

1. Find your LAN IP:
   ```bash
   # macOS
   ipconfig getifaddr en0

   # Linux
   hostname -I | awk '{print $1}'
   ```

2. Update the endpoint in
   [`app/src/main/java/com/paligot/otel/demo/MainApplication.kt`](../app/src/main/java/com/paligot/otel/demo/MainApplication.kt):
   ```kotlin
   val otlpExporter = OtlpHttpSpanExporter.builder()
       .setEndpoint("http://<YOUR_LAN_IP>:4318/v1/traces")
       .build()
   ```

3. Make sure your device and your machine are on the same Wi-Fi network.
4. If you have a macOS firewall, allow incoming connections on port `4318`.

---

## Step 5 – Confirm spans are reaching the Collector

After running the app, query the Collector's self-metrics endpoint:

```bash
curl -s http://localhost:8888/metrics | grep otelcol_receiver_accepted_spans
```

You should see a non-zero value, for example:

```
otelcol_receiver_accepted_spans{receiver="otlp",transport="http"} 47
```

You can also tail the Collector logs for live span confirmations:

```bash
docker compose logs -f otel-collector
```

Each received batch prints a line similar to:

```
Traces   #0: ... service.name: Str(com.paligot.otel-demo)
```

---

## Step 6 – Explore traces in Grafana

1. In Grafana, go to **Explore** (compass icon in the left sidebar).
2. Make sure the data source is set to **Tempo**.
3. In the **Search** tab, set:
   - **Service name** → `com.paligot.otel-demo`
4. Click **Run query**.  
   Traces for every ViewModel operation and HTTP call appear in the results.

### Useful TraceQL queries

Open the **TraceQL** tab and try these:

```
# All spans from the app
{ resource.service.name = "com.paligot.otel-demo" }

# Only ViewModel-level load operations (slower than 500ms)
{ resource.service.name = "com.paligot.otel-demo" && name =~ ".*\\.load" && duration > 500ms }

# HTTP calls only
{ resource.service.name = "com.paligot.otel-demo" && span.http.request.method != "" }

# Traces that contain an error
{ resource.service.name = "com.paligot.otel-demo" && status = error }
```

---

## Step 7 – Open the pre-built dashboard

1. In Grafana, go to **Dashboards** (grid icon in the left sidebar).
2. Open the **OTel Demo** folder.
3. Open **OTel Demo – Traces**.

The dashboard contains two panels:
- **Recent Traces** — a live list of all incoming traces, sortable by duration.
- **Service Graph** — a dependency map auto-generated from the parent/child span
  relationships (ViewModel → Repository → HTTP).

---

## Stopping the stack

```bash
docker compose down
```

Trace data is persisted in named Docker volumes (`tempo-data`, `grafana-data`).
To also delete stored data:

```bash
docker compose down -v
```

---

## Ports reference

| Port | Service | Purpose |
|------|---------|---------|
| `4317` | OTel Collector | OTLP gRPC receiver |
| `4318` | OTel Collector | OTLP HTTP receiver (used by the app) |
| `8888` | OTel Collector | Self-metrics / health check |
| `3200` | Grafana Tempo | HTTP API (queried by Grafana) |
| `3000` | Grafana | Web UI |

---

## Troubleshooting

**Spans reach the Collector but not Tempo**  
Check Tempo logs for ingestion errors:
```bash
docker compose logs tempo
```

**`otelcol_receiver_accepted_spans` stays at 0**  
- Confirm port `4318` is not occupied by another process: `lsof -i :4318`
- On a physical device, verify the LAN IP is correct and there is no firewall blocking the port.

**Grafana shows "No data"**  
- Ensure the Tempo data source URL is `http://tempo:3200` (container-internal DNS, not `localhost`).
- Re-run the provisioning check: **Connections → Data sources → Tempo → Save & test**.

**Collector container exits immediately**  
The config file failed to parse. Validate it:
```bash
docker compose logs otel-collector
```
