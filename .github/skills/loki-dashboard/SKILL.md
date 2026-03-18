---
name: loki-dashboard
description: "Create or edit Grafana dashboard panels that query Loki logs. Use when: adding Loki panels, writing LogQL queries, building log-based SLO panels, or filtering structured metadata. Covers OTLP structured metadata patterns and known pitfalls for this project's Loki setup."
---

# Loki Dashboard Panels

## When to Use

- Creating or editing Grafana dashboard JSON files that query Loki
- Writing LogQL queries for panels
- Debugging empty Loki query results
- Building SLO panels from structured logs

## Infrastructure

- **Loki version**: 3.5.0
- **Datasource UID**: `loki`
- **Config**: `observability/loki/loki.yaml`
- **Dashboard location**: `observability/grafana/dashboards/*.json`
- **Reference dashboard**: `observability/grafana/dashboards/app-startup.json`

## Critical Rules — Structured Metadata

Loki receives logs via OTLP from the OpenTelemetry Collector. With OTLP ingestion, **all OTel attributes become structured metadata**, NOT stream labels.

1. **Only `service_name` is a stream label** — it can go in `{service_name="..."}` label matchers
2. **All other attributes must use pipeline filters** — use `| key="value"` syntax, NOT `{key="value"}`
3. **Duration is stored as a string** — you need `line_format` + `pattern` to extract and compare numerically

### Correct vs Incorrect

```
# CORRECT — service_name as label, scope_name as pipeline filter
{service_name="com.paligot.otel-demo"} | scope_name="AppStartupLifecycle" | start_type="cold"

# WRONG — scope_name is NOT a stream label
{service_name="com.paligot.otel-demo", scope_name="AppStartupLifecycle"}
```

## Panel Patterns

### SLO Percentage Panel

Computes the ratio of logs meeting a duration threshold. Used for app startup SLOs:

```json
{
  "type": "stat",
  "datasource": { "type": "loki", "uid": "loki" },
  "targets": [{
    "refId": "A",
    "datasource": { "type": "loki", "uid": "loki" },
    "expr": "(sum(count_over_time({service_name=\"com.paligot.otel-demo\"} | scope_name=\"AppStartupLifecycle\" | start_type=\"cold\" | line_format \"{{.duration}}\" | pattern \"<dur>\" | dur < 1500 [$__range])) or vector(0)) / sum(count_over_time({service_name=\"com.paligot.otel-demo\"} | scope_name=\"AppStartupLifecycle\" | start_type=\"cold\" [$__range]))",
    "queryType": "range"
  }],
  "fieldConfig": {
    "defaults": {
      "unit": "percentunit",
      "thresholds": {
        "mode": "absolute",
        "steps": [
          { "color": "red", "value": null },
          { "color": "orange", "value": 0.9 },
          { "color": "green", "value": 0.95 }
        ]
      }
    }
  },
  "options": {
    "reduceOptions": { "calcs": ["lastNotNull"] },
    "colorMode": "background",
    "graphMode": "none",
    "justifyMode": "center"
  }
}
```

### Duration Extraction Pattern

Duration is a string attribute. To compare it numerically:

```
| line_format "{{.duration}}"   # extract duration attribute into the log line
| pattern "<dur>"               # parse it into a variable
| dur < 1500                    # numeric comparison (ms)
```

### Counting Logs

```
count_over_time({service_name="com.paligot.otel-demo"} | scope_name="AppStartupLifecycle" [$__range])
```

### Filtering by Multiple Attributes

Chain pipeline filters — each `|` narrows the result:

```
{service_name="com.paligot.otel-demo"}
  | scope_name="AppStartupLifecycle"
  | start_type="cold"
  | app_version="1.0.0"
```

## Available Structured Metadata Attributes

These are attribute keys emitted by the Android app (check the lifecycle classes for the latest list):

| Attribute | Source | Example |
|-----------|--------|---------|
| `scope_name` | OTel logger name | `AppStartupLifecycle` |
| `start_type` | App startup type | `cold`, `warm`, `hot` |
| `duration` | Duration in ms (string) | `1234` |
| `ttid_duration` | Time-to-initial-display | `890` |
| `app_version` | App version name | `1.0.0` |
| `device_api_level` | Android API level | `35` |

## Workflow

1. Write the LogQL query
2. Test via API:
   ```bash
   curl -s "http://localhost:3100/loki/api/v1/query_range" \
     --data-urlencode "query={service_name=\"com.paligot.otel-demo\"} | scope_name=\"AppStartupLifecycle\"" \
     --data-urlencode "start=$(date -v-1H +%s)" \
     --data-urlencode "end=$(date +%s)" \
     --data-urlencode "limit=10" | python3 -m json.tool | head -40
   ```
3. Edit the dashboard JSON
4. Restart Grafana: `docker compose restart grafana`
5. Verify in the Grafana UI
