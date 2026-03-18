package com.paligot.otel.demo.otel

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.use

fun Tracer.span(
    name: String,
    attributes: Attributes = Attributes.empty(),
    kind: SpanKind = SpanKind.INTERNAL,
    block: () -> Unit,
) {
    val span = this
        .spanBuilder(name)
        .setSpanKind(kind)
        .setStartTimestamp(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .setAllAttributes(attributes)
        .startSpan()
    try {
        span.makeCurrent().use {
            block()
        }
    } catch (error: Throwable) {
        span.recordException(error)
        throw error
    } finally {
        span.end(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    }
}

fun <T> Tracer.span(
    name: String,
    attributes: Attributes = Attributes.empty(),
    kind: SpanKind = SpanKind.INTERNAL,
    block: () -> T,
): T {
    val span = this
        .spanBuilder(name)
        .setSpanKind(kind)
        .setStartTimestamp(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .setAllAttributes(attributes)
        .startSpan()
    return try {
        span.makeCurrent().use {
            block()
        }
    } catch (error: Throwable) {
        span.recordException(error)
        throw error
    } finally {
        span.end(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    }
}

suspend fun <T> Tracer.suspendSpan(
    name: String,
    attributes: Attributes = Attributes.empty(),
    kind: SpanKind = SpanKind.INTERNAL,
    block: suspend () -> T,
): T {
    val span = this
        .spanBuilder(name)
        .setSpanKind(kind)
        .setStartTimestamp(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .setAllAttributes(attributes)
        .startSpan()
    val context = Context.current().with(span)
    return try {
        withContext(context.asContextElement()) {
            block()
        }
    } catch (error: Throwable) {
        span.recordException(error)
        throw error
    } finally {
        span.end(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    }
}
