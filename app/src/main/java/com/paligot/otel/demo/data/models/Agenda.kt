package com.paligot.otel.demo.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgendaResponse(
    val schedules: List<AgendaSchedule> = emptyList(),
    val sessions: List<AgendaSessionRaw> = emptyList(),
    val formats: List<AgendaFormat> = emptyList(),
    val categories: List<AgendaCategory> = emptyList(),
    val speakers: List<AgendaSpeaker> = emptyList(),
)

@Serializable
data class AgendaSchedule(
    val id: String,
    val order: Int = 0,
    val date: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val room: String,
    @SerialName("session_id") val sessionId: String,
)

/** Single flat DTO for both `talk-session` and `event-session` types. */
@Serializable
data class AgendaSessionRaw(
    val type: String,
    val id: String,
    val title: String,
    val level: String = "",
    val abstract: String = "",
    @SerialName("categoryId") val categoryId: String = "",
    @SerialName("formatId") val formatId: String = "",
    val language: String = "",
    val speakers: List<String> = emptyList(),
    val description: String? = null,
    @SerialName("open_feedback") val openFeedback: String? = null,
    @SerialName("link_slides") val linkSlides: String? = null,
    @SerialName("link_replay") val linkReplay: String? = null,
)

@Serializable
data class AgendaFormat(
    val id: String,
    val name: String,
    val time: Int = 0,
)

@Serializable
data class AgendaCategory(
    val id: String,
    val name: String,
    val color: String = "",
    val icon: String = "",
)

@Serializable
data class AgendaSpeaker(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val pronouns: String? = null,
    val bio: String? = null,
    @SerialName("job_title") val jobTitle: String? = null,
    val company: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    val website: String? = null,
    val twitter: String? = null,
    val mastodon: String? = null,
    val github: String? = null,
    val linkedin: String? = null,
)
