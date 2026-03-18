package com.paligot.otel.demo.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.FrameMetrics
import android.view.Window
import io.opentelemetry.api.OpenTelemetry
import java.time.Instant

/**
 * Detects janky frames using [Window.OnFrameMetricsAvailableListener] and emits one OTel span
 * per janky frame. A frame is considered janky when its total CPU duration exceeds one frame
 * budget (1 000 000 000 / [frameRateHz] nanoseconds).
 *
 * Spans arrive in Tempo under the name "jank.frame" and carry:
 *   - screen.name            — current Compose route (updated via [currentScreen])
 *   - frame.total_duration_ms
 *   - frame.draw_duration_ms
 *   - frame.layout_measure_duration_ms
 *   - frame.dropped_count    — frames skipped since the previous delivery
 *
 * Registration must be done at Application level (Application.registerActivityLifecycleCallbacks)
 * to avoid the API-29 requirement of Activity.registerActivityLifecycleCallbacks.
 */
class JankDetectionLifecycle(
    private val openTelemetry: OpenTelemetry,
    private val currentScreen: () -> String = { "unknown" },
    private val frameRateHz: Int = 60,
) : Application.ActivityLifecycleCallbacks {

    private val tracer = openTelemetry.getTracer("JankDetection")
    private val frameDeadlineNs = 2 * 1_000_000_000L / frameRateHz

    // Process frame metrics on a dedicated background thread to avoid polluting the main thread.
    private val handlerThread = HandlerThread("jank-metrics").also { it.start() }
    private val handler = Handler(handlerThread.looper)

    private val listener = Window.OnFrameMetricsAvailableListener { _, frameMetrics, droppedCount ->
        val totalNs = frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)
        if (totalNs <= frameDeadlineNs) return@OnFrameMetricsAvailableListener

        // FrameMetrics timestamps are uptime-based (System.nanoTime). Convert to epoch.
        val vsyncUptimeNs = frameMetrics.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP)
        val startEpochNs = System.currentTimeMillis() * 1_000_000L - (System.nanoTime() - vsyncUptimeNs)

        val startInstant = Instant.ofEpochSecond(0, startEpochNs)
        val endInstant = Instant.ofEpochSecond(0, startEpochNs + totalNs)

        tracer.spanBuilder("jank.frame")
            .setStartTimestamp(startInstant)
            .setAttribute("screen.name", currentScreen())
            .setAttribute("frame.total_duration_ms", totalNs / 1_000_000.0)
            .setAttribute("frame.draw_duration_ms", frameMetrics.getMetric(FrameMetrics.DRAW_DURATION) / 1_000_000.0)
            .setAttribute("frame.layout_measure_duration_ms", frameMetrics.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION) / 1_000_000.0)
            .setAttribute("frame.dropped_count", droppedCount.toLong())
            .startSpan()
            .end(endInstant)
    }

    override fun onActivityResumed(activity: Activity) {
        activity.window.addOnFrameMetricsAvailableListener(listener, handler)
    }

    override fun onActivityPaused(activity: Activity) {
        activity.window.removeOnFrameMetricsAvailableListener(listener)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}
