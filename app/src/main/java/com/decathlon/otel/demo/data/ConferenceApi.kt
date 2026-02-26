package com.decathlon.otel.demo.data

import com.decathlon.otel.demo.data.models.EventList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import kotlinx.serialization.json.Json

class ConferenceApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun fetchEventList(): EventList = client.get("$baseUrl/events").body()

    companion object {
        fun create(telemetry: OpenTelemetry, baseUrl: String, enableNetworkLogs: Boolean): ConferenceApi =
            ConferenceApi(
                baseUrl = baseUrl,
                client = HttpClient(CIO) {
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
                    install(KtorClientTelemetry) {
                        setOpenTelemetry(telemetry)
                    }
                }
            )
    }
}
