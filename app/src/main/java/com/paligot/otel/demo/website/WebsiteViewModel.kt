package com.paligot.otel.demo.website

import androidx.lifecycle.ViewModel
import com.paligot.otel.demo.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WebsiteViewModel @Inject constructor(
    openTelemetry: OpenTelemetry,
) : ViewModel() {

    private val tracer = openTelemetry.getTracer("website-webview")
    private var currentSpan: Span? = null

    fun onPageStarted(url: String) {
        currentSpan = tracer.spanBuilder("webview.page.load")
            .setSpanKind(SpanKind.CLIENT)
            .setStartTimestamp(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setAllAttributes(
                Attributes.of(
                    AttributeKey.stringKey("webview.url"), url,
                    AttributeKey.booleanKey("webview.is_main_frame"), true,
                    AttributeKey.stringKey("app.version"), BuildConfig.VERSION_NAME,
                    AttributeKey.longKey("device.api_level"), android.os.Build.VERSION.SDK_INT.toLong(),
                )
            )
            .startSpan()
    }

    fun onPageFinished(url: String) {
        currentSpan?.apply {
            setAttribute("webview.url.final", url)
            end(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        }
        currentSpan = null
    }

    override fun onCleared() {
        super.onCleared()
        currentSpan?.end()
        currentSpan = null
    }
}
