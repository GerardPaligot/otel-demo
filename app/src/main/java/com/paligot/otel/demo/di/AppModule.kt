package com.paligot.otel.demo.di

import com.paligot.otel.demo.BuildConfig
import com.paligot.otel.demo.data.AgendaRepository
import com.paligot.otel.demo.data.ConferenceApi
import com.paligot.otel.demo.data.EventRepository
import com.paligot.otel.demo.data.PartnerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOpenTelemetry(): OpenTelemetry {
        val serviceNameResource = Resource.create(
            Attributes.of(AttributeKey.stringKey("service.name"), "com.paligot.otel-demo")
        )
        val resource = Resource.getDefault().merge(serviceNameResource)

        val otlpExporter = OtlpHttpSpanExporter.builder()
            .setEndpoint("${BuildConfig.BASE_URL_OTEL_COLLECTOR}/v1/traces")
            .build()

        val tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .addSpanProcessor(BatchSpanProcessor.builder(otlpExporter).build())
            .build()

        val otlpLogExporter = OtlpHttpLogRecordExporter.builder()
            .setEndpoint("${BuildConfig.BASE_URL_OTEL_COLLECTOR}/v1/logs")
            .build()

        val loggerProvider = SdkLoggerProvider.builder()
            .setResource(resource)
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(SystemOutLogRecordExporter.create()))
            .addLogRecordProcessor(BatchLogRecordProcessor.builder(otlpLogExporter).build())
            .build()

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setLoggerProvider(loggerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build()
    }

    @Provides
    @Singleton
    fun provideConferenceApi(openTelemetry: OpenTelemetry): ConferenceApi =
        ConferenceApi.create(openTelemetry, BuildConfig.BASE_URL, false)

    @Provides
    @Singleton
    fun provideEventRepository(openTelemetry: OpenTelemetry, api: ConferenceApi): EventRepository =
        EventRepository(openTelemetry, api)

    @Provides
    @Singleton
    fun provideAgendaRepository(openTelemetry: OpenTelemetry, api: ConferenceApi): AgendaRepository =
        AgendaRepository(openTelemetry, api)

    @Provides
    @Singleton
    fun providePartnerRepository(openTelemetry: OpenTelemetry, api: ConferenceApi): PartnerRepository =
        PartnerRepository(openTelemetry, api)
}
