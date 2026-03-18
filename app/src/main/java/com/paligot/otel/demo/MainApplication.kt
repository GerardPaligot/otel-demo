package com.paligot.otel.demo

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.paligot.otel.demo.lifecycle.AppStartupLifecycle
import com.paligot.otel.demo.lifecycle.JankDetectionLifecycle
import dagger.hilt.android.HiltAndroidApp
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), SingletonImageLoader.Factory {
    @Inject lateinit var openTelemetry: OpenTelemetry
    var currentScreen: String = "unknown"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            registerActivityLifecycleCallbacks(
                AppStartupLifecycle(
                    context = this,
                    openTelemetry = openTelemetry,
                )
            )
        }
        registerActivityLifecycleCallbacks(
            JankDetectionLifecycle(
                openTelemetry = openTelemetry,
                currentScreen = { currentScreen },
            )
        )
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(
                    KtorNetworkFetcherFactory(
                        httpClient = {
                            HttpClient(CIO) {
                                install(KtorClientTelemetry) {
                                    setOpenTelemetry(openTelemetry)
                                }
                            }
                        }
                    )
                )
            }
            .build()
}
