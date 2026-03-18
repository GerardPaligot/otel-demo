package com.paligot.otel.demo.data

import com.paligot.otel.demo.data.models.AgendaResponse
import com.paligot.otel.demo.data.models.Event
import com.paligot.otel.demo.data.models.EventList
import com.paligot.otel.demo.data.models.PartnerResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import kotlinx.serialization.json.Json

class ConferenceApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun fetchEventList(): EventList = client.get("$baseUrl/events").body()

    suspend fun fetchEvent(eventSlug: String): Event = client.get("$baseUrl/events/$eventSlug") {
        header("Accept", "application/json; version=3")
    }.body()

    suspend fun fetchAgenda(eventSlug: String): AgendaResponse =
        client.get("$baseUrl/events/$eventSlug/agenda") {
            header("Accept", "application/json; version=4")
        }.body()

    suspend fun fetchPartners(eventSlug: String): Map<String, List<PartnerResponse>> =
        client.get("$baseUrl/events/$eventSlug/partners").body()

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
