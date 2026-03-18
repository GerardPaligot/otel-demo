# OTel Demo

An Android demo app showcasing multi-signal OpenTelemetry instrumentation with a local observability stack.

## App

A Jetpack Compose conference app with 4 screens (Schedule, Speakers, Sponsors, Website), built with Ktor, Hilt and Coil.

## Instrumentation

| Signal | What | How |
|--------|------|-----|
| Traces | HTTP client calls | Ktor auto-instrumentation via `KtorClientTelemetry` |
| Traces | App startup (cold/warm/hot) | `ApplicationStartInfo` (API 35+) in `AppStartupLifecycle` |
| Traces | Jank frames | `OnFrameMetricsAvailableListener` in `JankDetectionLifecycle` |
| Traces | WebView page loads | `WebViewClient` callbacks in `WebsiteViewModel` |
| Traces | Data loading | Custom spans in repositories and ViewModels |
| Logs | Startup metrics | Structured log with type, duration, app version, API level |

All telemetry is exported via OTLP to a local OpenTelemetry Collector.

## Observability Stack

Started with `docker compose up`:

| Service | Image | Port |
|---------|-------|------|
| OTel Collector | `otel/opentelemetry-collector-contrib:0.123.0` | 4317 (gRPC), 4318 (HTTP) |
| Tempo | `grafana/tempo:2.7.2` | 3200 |
| Loki | `grafana/loki:3.5.0` | 3100 |
| Grafana | `grafana/grafana:11.6.0` | 3000 |

The Android app sends data to `http://10.0.2.2:4318` (emulator) — update to your LAN IP for a physical device.

## Grafana Dashboards

Four pre-provisioned dashboards at [http://localhost:3000](http://localhost:3000):

- **App Startup Time** — Cold/warm/hot start SLOs and duration breakdown
- **HTTP Client Performance** — Request counts, durations and error rates
- **Jank Detection** — Janky frames by screen, duration distribution
- **WebView Page Load** — Page load times and URL tracking

See [observability/OBSERVABILITY.md](observability/OBSERVABILITY.md) for setup details.

## License

    Copyright 2023 Gérard Paligot.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
