package com.paligot.otel.demo.sponsors

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── Sample data ───────────────────────────────────────────────────────────────

private val defaultSponsors = listOf(
    SponsorUi(
        name = "TechGiant",
        category = "Cloud Infrastructure",
        tier = SponsorTier.PLATINUM,
        logoColor = Color(0xFFEF5350),
    ),
    SponsorUi(
        name = "InnovateCorp",
        category = "AI Solutions",
        tier = SponsorTier.PLATINUM,
        logoColor = Color(0xFFEC407A),
    ),
    SponsorUi(
        name = "DevToolz",
        category = "Developer Productivity",
        tier = SponsorTier.GOLD,
        logoColor = Color(0xFFEF5350),
    ),
    SponsorUi(
        name = "CyberSafe",
        category = "Security First",
        tier = SponsorTier.GOLD,
        logoColor = Color(0xFF212121),
        logoTextColor = Color(0xFFEF5350),
    ),
    SponsorUi(
        name = "DataFlow",
        category = "Analytics Platform",
        tier = SponsorTier.GOLD,
        logoColor = Color(0xFFFF7043),
    ),
    SponsorUi(
        name = "CloudScale",
        category = "Serverless Computing",
        tier = SponsorTier.GOLD,
        logoColor = Color(0xFFEF5350),
    ),
    SponsorUi(
        name = "Sponsor 1",
        category = "",
        tier = SponsorTier.SILVER,
        logoColor = Color(0xFF4A148C).copy(alpha = .6f),
    ),
    SponsorUi(
        name = "Ponko",
        category = "",
        tier = SponsorTier.SILVER,
        logoColor = Color(0xFF1A1A2E),
    ),
    SponsorUi(
        name = "NNI",
        category = "",
        tier = SponsorTier.SILVER,
        logoColor = Color(0xFF9E9E9E).copy(alpha = .4f),
    ),
    SponsorUi(
        name = "Sponsor 4",
        category = "",
        tier = SponsorTier.SILVER,
        logoColor = Color(0xFF4A148C).copy(alpha = .4f),
    ),
    SponsorUi(
        name = "Sponsor 5",
        category = "",
        tier = SponsorTier.SILVER,
        logoColor = Color(0xFF880E4F).copy(alpha = .5f),
    ),
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class SponsorsUiState(
    val searchQuery: String = "",
    val allSponsors: List<SponsorUi> = defaultSponsors,
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
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class SponsorsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SponsorsUiState())
    val uiState: StateFlow<SponsorsUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
