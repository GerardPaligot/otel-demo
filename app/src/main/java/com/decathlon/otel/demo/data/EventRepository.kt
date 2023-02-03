package com.decathlon.otel.demo.data

import com.decathlon.otel.demo.data.models.EventItemList
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import java.util.concurrent.TimeUnit

class EventRepository(
    private val telemetry: OpenTelemetry,
    private val api: ConferenceApi
) {
    private val tracer = telemetry.getTracer("event-repository")
    suspend fun events(): List<EventItemList> {
        val span = tracer
            .spanBuilder("fetch-events")
            .setSpanKind(SpanKind.CLIENT)
            .setStartTimestamp(System.nanoTime(), TimeUnit.NANOSECONDS)
            .startSpan()
        try {
            span.makeCurrent().use {
                val events = api.fetchEventList()
                return events.future + events.past
            }
        } catch (error: Throwable) {
            span.recordException(error)
            throw error
        } finally {
            span.end(System.nanoTime(), TimeUnit.NANOSECONDS)
        }
    }
}
