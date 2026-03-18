package com.paligot.otel.demo.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PartnerMediaPngs(
    @SerialName("_250") val p250: String = "",
    @SerialName("_500") val p500: String = "",
    @SerialName("_1000") val p1000: String = "",
)

@Serializable
data class PartnerMedia(
    val svg: String = "",
    val pngs: PartnerMediaPngs = PartnerMediaPngs(),
)

@Serializable
data class PartnerAddress(
    val formatted: List<String> = emptyList(),
    val address: String? = null,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val city: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class PartnerResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("logo_url") val logoUrl: String = "",
    val media: PartnerMedia = PartnerMedia(),
    @SerialName("site_url") val siteUrl: String? = null,
    @SerialName("twitter_url") val twitterUrl: String? = null,
    @SerialName("twitter_message") val twitterMessage: String? = null,
    @SerialName("linkedin_url") val linkedinUrl: String? = null,
    @SerialName("linkedin_message") val linkedinMessage: String? = null,
    val address: PartnerAddress? = null,
)
