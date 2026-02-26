package com.decathlon.otel.demo

import android.app.Application
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor

class MainApplication : Application() {
    lateinit var openTelemetry: OpenTelemetry

    override fun onCreate() {
        super.onCreate()
        openTelemetry = openTelemetry()
    }

    private fun openTelemetry(): OpenTelemetry {
        val logExporter = LoggingSpanExporter.create()
        val serviceNameResource =
            Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), "com.decathlon.otel-demo"))
        val tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(logExporter))
            .setResource(Resource.getDefault().merge(serviceNameResource))
            .build()
        return OpenTelemetrySdk
            .builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build()
    }
}
