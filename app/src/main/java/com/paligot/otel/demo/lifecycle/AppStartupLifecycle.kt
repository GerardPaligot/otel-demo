package com.paligot.otel.demo.lifecycle

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.ApplicationStartInfo
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.paligot.otel.demo.BuildConfig
import com.paligot.otel.demo.otel.log
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.util.function.Consumer

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class AppStartupLifecycle(
    private val context: Context,
    private val openTelemetry: OpenTelemetry,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Application.ActivityLifecycleCallbacks {
    private val activityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    private val logger by lazy {
        openTelemetry.logsBridge.get("AppStartupLifecycle")
    }

    private val listener = Consumer<ApplicationStartInfo> {
        val startupTimes = it.startupTimestamps
        val launchDuration = startupTimes[ApplicationStartInfo.START_TIMESTAMP_LAUNCH]
            ?.let { it / 1_000_000 }
            ?: return@Consumer
        val ttidDuration = startupTimes[ApplicationStartInfo.START_TIMESTAMP_FIRST_FRAME]
            ?.let { it / 1_000_000 }
            ?: return@Consumer
        val startupType = when (it.startType) {
            ApplicationStartInfo.START_TYPE_COLD -> "cold"
            ApplicationStartInfo.START_TYPE_WARM -> "warm"
            ApplicationStartInfo.START_TYPE_HOT -> "hot"
            else -> "unknown"
        }
        logger.log(
            message = "Application start info",
            attributes = Attributes
                .of(
                    AttributeKey.stringKey("start.type"), startupType,
                    AttributeKey.stringKey("duration"), (ttidDuration - launchDuration).toString(),
                    AttributeKey.stringKey("app.version"), BuildConfig.VERSION_NAME,
                    AttributeKey.longKey("device.api_level"), android.os.Build.VERSION.SDK_INT.toLong(),
                )
        )
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        activityManager.addApplicationStartInfoCompletionListener(
            dispatcher.asExecutor(),
            listener
        )
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        activityManager.removeApplicationStartInfoCompletionListener(listener)
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
    }
}