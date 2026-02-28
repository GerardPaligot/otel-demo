package com.paligot.otel.demo.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ── Sample data ───────────────────────────────────────────────────────────────

private val defaultUpcomingSessions = listOf(
    UpcomingSessionUi(
        time = "10:00\nAM",
        title = "Web3 & Decentralization",
        room = "Room 304",
        format = "Workshop",
        speakerInitials = listOf("CK", "OL"),
        speakerColors = listOf(Color(0xFFFF7043), Color(0xFF1565C0)),
    ),
    UpcomingSessionUi(
        time = "11:30\nAM",
        title = "Sustainable Tech Practices",
        room = "Main Auditorium",
        format = "Panel",
        speakerInitials = listOf("MR"),
        speakerColors = listOf(Color(0xFF6A1B9A)),
    ),
)

private val defaultExploreCards = listOf(
    ExploreCardUi("Networking", "Events", Color(0xFF263238)),
    ExploreCardUi("Expo Hall", "Booths", Color(0xFF1B5E20)),
)

private val defaultGoldSponsors = listOf(
    SponsorLogoUi(
        name = "Acme",
        accentColor = Color(0xFF3F51B5),
        bgColor = Color(0xFF1A1C26),
        textColor = Color.White,
    ),
    SponsorLogoUi(
        name = "BOLT",
        bgColor = Color(0xFF1A1C26),
        textColor = Color.White,
    ),
    SponsorLogoUi(
        name = "sphere",
        bgColor = Color(0xFF1A1C26),
        textColor = Color.White,
        isItalic = true,
    ),
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class HomeUiState(
    val upcomingSessions: List<UpcomingSessionUi> = defaultUpcomingSessions,
    val exploreCards: List<ExploreCardUi> = defaultExploreCards,
    val goldSponsors: List<SponsorLogoUi> = defaultGoldSponsors,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
