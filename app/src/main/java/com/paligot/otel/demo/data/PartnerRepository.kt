package com.paligot.otel.demo.data

import com.paligot.otel.demo.data.models.PartnerResponse
import com.paligot.otel.demo.otel.suspendSpan
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanKind

class PartnerRepository(
    telemetry: OpenTelemetry,
    private val api: ConferenceApi,
) {
    private val tracer = telemetry.getTracer("partner-repository")

    suspend fun partners(eventSlug: String): Map<String, List<PartnerResponse>> {
        return tracer.suspendSpan(
            name = "fetch-partners",
            kind = SpanKind.CLIENT,
            attributes = Attributes.of(
                AttributeKey.stringKey("event.slug"), eventSlug,
            )
        ) {
            api.fetchPartners(eventSlug)
        }
    }
}
