package com.paligot.otel.demo.data

import com.paligot.otel.demo.data.models.Event
import com.paligot.otel.demo.otel.suspendSpan
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanKind

class EventRepository(
    telemetry: OpenTelemetry,
    private val api: ConferenceApi
) {
    private val tracer = telemetry.getTracer("event-repository")

    suspend fun event(eventSlug: String): Event {
        return tracer.suspendSpan(
            name = "fetch-event",
            kind = SpanKind.CLIENT,
            attributes = Attributes.of(
                AttributeKey.stringKey("event.slug"), eventSlug
            )
        ) {
            api.fetchEvent(eventSlug)
        }
    }
}
