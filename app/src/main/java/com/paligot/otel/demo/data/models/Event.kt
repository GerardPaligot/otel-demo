package com.paligot.otel.demo.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val name: String,
    val address: EventAddress? = null,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val menus: List<EventMenu> = emptyList(),
    val coc: String? = null,
    @SerialName("openfeedback_project_id") val openfeedbackProjectId: String? = null,
    val features: EventFeatures = EventFeatures(),
    @SerialName("contact_phone") val contactPhone: String? = null,
    @SerialName("contact_email") val contactEmail: String? = null,
    @SerialName("twitter_url") val twitterUrl: String? = null,
    @SerialName("linkedin_url") val linkedinUrl: String? = null,
    @SerialName("faq_link") val faqLink: String? = null,
    @SerialName("code_of_conduct_link") val codeOfConductLink: String? = null,
    @SerialName("updated_at") val updatedAt: Long = 0L,
)

@Serializable
data class EventAddress(
    val formatted: List<String> = emptyList(),
    val address: String? = null,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val city: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class EventMenu(
    val name: String,
    val dish: String,
    val accompaniment: String,
    val dessert: String,
)

@Serializable
data class EventFeatures(
    @SerialName("has_networking") val hasNetworking: Boolean = false,
    @SerialName("has_speaker_list") val hasSpeakerList: Boolean = false,
    @SerialName("has_partner_list") val hasPartnerList: Boolean = false,
    @SerialName("has_menus") val hasMenus: Boolean = false,
    @SerialName("has_qanda") val hasQanda: Boolean = false,
    @SerialName("has_billet_web_ticket") val hasBilletWebTicket: Boolean = false,
)
