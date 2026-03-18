package com.paligot.otel.demo.sponsors

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paligot.otel.demo.BuildConfig
import com.paligot.otel.demo.data.PartnerRepository
import com.paligot.otel.demo.otel.span
import com.paligot.otel.demo.otel.suspendSpan
import dagger.hilt.android.lifecycle.HiltViewModel
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

// ── UI state ──────────────────────────────────────────────────────────────────

data class SponsorsUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val allSponsors: List<SponsorUi> = emptyList(),
) {
    private val filteredSponsors: List<SponsorUi>
        get() = if (searchQuery.isBlank()) allSponsors
        else allSponsors.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }

    val platinumSponsors: List<SponsorUi>
        get() = filteredSponsors.filter { it.tier == SponsorTier.PLATINUM }

    val goldSponsors: List<SponsorUi>
        get() = filteredSponsors.filter { it.tier == SponsorTier.GOLD }

    val silverSponsors: List<SponsorUi>
        get() = filteredSponsors.filter { it.tier == SponsorTier.SILVER }

    val bronzeSponsors: List<SponsorUi>
        get() = filteredSponsors.filter { it.tier == SponsorTier.BRONZE }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class SponsorsViewModel @Inject constructor(
    openTelemetry: OpenTelemetry,
    private val repository: PartnerRepository,
) : ViewModel() {

    private val tracer = openTelemetry.getTracer("sponsors-viewmodel")

    private val _uiState = MutableStateFlow(SponsorsUiState())
    val uiState: StateFlow<SponsorsUiState> = _uiState.asStateFlow()

    init {
        loadPartners(BuildConfig.EVENT_ID)
    }

    fun onSearchQueryChange(query: String) {
        tracer.span(
            name = "sponsors.search",
            attributes = Attributes.of(
                AttributeKey.stringKey("search.query"), query,
            ),
        ) {
            _uiState.update { it.copy(searchQuery = query) }
        }
    }

    private fun loadPartners(eventSlug: String) {
        viewModelScope.launch {
            tracer.suspendSpan(
                name = "sponsors.load",
                attributes = Attributes.of(
                    AttributeKey.stringKey("event.slug"), eventSlug,
                ),
            ) {
                val partnersByTier = repository.partners(eventSlug)
                val sponsors = partnersByTier.flatMap { (tierKey, partners) ->
                    val tier = tierKey.toSponsorTier() ?: return@flatMap emptyList()
                    partners.map { partner ->
                        SponsorUi(
                            name = partner.name,
                            category = partner.description
                                .orEmpty()
                                .lines()
                                .firstOrNull { it.isNotBlank() }
                                .orEmpty()
                                .take(60)
                                .trim(),
                            tier = tier,
                            logoColor = partnerAvatarColor(partner.name),
                            websiteUrl = partner.siteUrl.orEmpty(),
                            logoUrl = partner.media.pngs.p250,
                        )
                    }
                }
                _uiState.update { it.copy(isLoading = false, allSponsors = sponsors) }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun String.toSponsorTier(): SponsorTier? = when (this.lowercase()) {
    "gold" -> SponsorTier.GOLD
    "silver" -> SponsorTier.SILVER
    "bronze" -> SponsorTier.BRONZE
    else -> null
}

private val avatarPalette = listOf(
    Color(0xFF1565C0), Color(0xFF6A1B9A), Color(0xFF283593),
    Color(0xFF00695C), Color(0xFFBF360C), Color(0xFF37474F),
    Color(0xFF4527A0), Color(0xFF0D47A1), Color(0xFF1B5E20),
    Color(0xFF880E4F), Color(0xFF3E2723), Color(0xFF006064),
)

private fun partnerAvatarColor(name: String): Color =
    avatarPalette[abs(name.hashCode()) % avatarPalette.size]
