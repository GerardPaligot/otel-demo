package com.decathlon.otel.demo.data

import com.decathlon.otel.demo.data.models.EventList
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor

class ConferenceApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun fetchEventList(): EventList = client.get("$baseUrl/events").body()

    companion object {
        fun create(telemetry: OpenTelemetry, baseUrl: String, enableNetworkLogs: Boolean): ConferenceApi =
            ConferenceApi(
                baseUrl = baseUrl,
                client = HttpClient(
                    OkHttp.create {
                        this.config {
                            interceptors().add(0, OkHttpTelemetry.builder(telemetry).build().newInterceptor())
                            addInterceptor(HttpLoggingInterceptor())
                        }
                    }
                ) {
                    install(
                        ContentNegotiation
                    ) {
                        json(
                            Json {
                                isLenient = true
                                ignoreUnknownKeys = true
                            }
                        )
                    }
                    if (enableNetworkLogs) {
                        install(
                            Logging
                        ) {
                            logger = Logger.DEFAULT
                            level = LogLevel.ALL
                        }
                    }
                }
            )
    }
}
