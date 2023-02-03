package com.decathlon.otel.demo.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventList(
    val future: List<EventItemList>,
    val past: List<EventItemList>
)

@Serializable
data class EventItemList(
    val id: String,
    val name: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
)
