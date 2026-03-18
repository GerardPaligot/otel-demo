---
name: tempo-dashboard
description: "Create or edit Grafana dashboard panels that query Tempo traces. Use when: adding Tempo panels, writing TraceQL queries, building trace tables, stat panels, or groupBy aggregations. Covers working patterns and known pitfalls for this project's Tempo setup."
---

# Tempo Dashboard Panels

## When to Use

- Creating or editing Grafana dashboard JSON files that query Tempo
- Writing TraceQL queries for panels
- Debugging empty or broken Tempo panels

## Infrastructure

- **Tempo version**: 2.7.2
- **Datasource UID**: `tempo`
- **Grafana datasource config**: `observability/grafana/datasources/datasources.yaml`
- **Dashboard location**: `observability/grafana/dashboards/*.json`
- **Reference dashboard**: `observability/grafana/dashboards/webview-page-load.json` — always consult this file for working patterns

## Critical Rules

1. **Always use `"queryType": "traceql"`** — `"queryType": "search"` is NOT supported and produces empty results
2. **Never use `"type": "traces"` panels** — use `"type": "table"` or `"type": "stat"` instead
3. **TraceQL metrics functions don't work** — `count_over_time()`, `quantile_over_time()`, `max_over_time()`, `histogram_over_time()` all return empty series. Use plain TraceQL + Grafana transformations instead
4. **Always verify queries via API before editing dashboard JSON**:
   ```bash
   curl -s "http://localhost:3200/api/search?q=$(python3 -c 'import urllib.parse; print(urllib.parse.quote(QUERY))')" | python3 -m json.tool | head -30
   ```

## Panel Patterns

### Stat Panel (count with threshold)

Use for SLO-style counters (e.g., "Requests under 500ms"):

```json
{
  "type": "stat",
  "datasource": { "type": "tempo", "uid": "tempo" },
  "targets": [{
    "refId": "A",
    "datasource": { "type": "tempo", "uid": "tempo" },
    "queryType": "traceql",
    "query": "{ name = \"span.name\" && duration < 500ms }"
  }],
  "transformations": [{
    "id": "reduce",
    "options": { "reducers": ["count"], "includeTimeField": false }
  }],
  "fieldConfig": {
    "defaults": {
      "unit": "none",
      "thresholds": {
        "mode": "absolute",
        "steps": [{ "color": "green", "value": null }]
      }
    }
  },
  "options": {
    "reduceOptions": { "calcs": ["lastNotNull"] },
    "orientation": "auto",
    "colorMode": "background",
    "graphMode": "none",
    "justifyMode": "center"
  }
}
```

### Table Panel with groupBy Aggregations

Use for breakdown tables (e.g., "Duration by method"):

```json
{
  "type": "table",
  "datasource": { "type": "tempo", "uid": "tempo" },
  "targets": [{
    "refId": "A",
    "datasource": { "type": "tempo", "uid": "tempo" },
    "queryType": "traceql",
    "query": "{ name = \"span.name\" && .attribute != nil } | select(.attribute, duration)",
    "limit": 100,
    "tableType": "spans",
    "spss": 10
  }],
  "transformations": [{
    "id": "groupBy",
    "options": {
      "fields": {
        "attribute": { "aggregations": [], "operation": "groupby" },
        "duration": { "aggregations": ["count", "mean", "max"], "operation": "aggregate" }
      }
    }
  }],
  "fieldConfig": {
    "defaults": {},
    "overrides": [
      { "matcher": { "id": "byName", "options": "duration (mean)" }, "properties": [{ "id": "unit", "value": "ns" }] },
      { "matcher": { "id": "byName", "options": "duration (max)" }, "properties": [{ "id": "unit", "value": "ns" }] }
    ]
  }
}
```

Key fields for groupBy queries:
- `"tableType": "spans"` — required to get span-level fields
- `"spss": 10` — spans per span set
- `| select(.attr, duration)` — required to expose fields for groupBy

### Trace Listing Table

Use for raw span exploration:

```json
{
  "type": "table",
  "datasource": { "type": "tempo", "uid": "tempo" },
  "targets": [{
    "refId": "A",
    "datasource": { "type": "tempo", "uid": "tempo" },
    "queryType": "traceql",
    "query": "{ name = \"span.name\" }",
    "limit": 100
  }],
  "options": {
    "showHeader": true,
    "sortBy": [{ "displayName": "Start time", "desc": true }],
    "footer": { "show": true, "reducer": ["count"], "countRows": true }
  }
}
```

## TraceQL Syntax

```
{ name = "span.name" }                              # match by span name
{ kind = client }                                     # match by span kind
{ duration > 500ms }                                  # filter by duration
{ .http.request.method = "GET" }                      # span attribute (prefix with .)
{ name = "span" && .attr != nil }                     # attribute existence check
{ name = "span" && duration < 1s && .room != nil }    # combine filters
```

## Workflow

1. Write the TraceQL query
2. Test it via API: `curl "http://localhost:3200/api/search?q=..."`
3. Edit the dashboard JSON
4. Restart Grafana: `docker compose restart grafana`
5. Verify in the Grafana UI
