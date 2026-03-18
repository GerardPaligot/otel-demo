package com.paligot.otel.demo.data

import com.paligot.otel.demo.data.models.AgendaResponse
import com.paligot.otel.demo.otel.suspendSpan
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AgendaRepository(
    telemetry: OpenTelemetry,
    private val api: ConferenceApi,
) {
    private val tracer = telemetry.getTracer("agenda-repository")

    suspend fun agenda(eventSlug: String): AgendaResponse {
        return tracer.suspendSpan("fetch-agenda") {
            api.fetchAgenda(eventSlug)
        }
    }
}
