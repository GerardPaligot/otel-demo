package com.paligot.otel.demo.speakers

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── Sample data ───────────────────────────────────────────────────────────────

private val defaultSpeakers = listOf(
    SpeakerUi(
        name = "Alice Johnson",
        role = "CTO",
        company = "TechCorp",
        topics = listOf("Keynote", "Leadership"),
        isFeatured = true,
        featuredCategory = "Keynote",
        avatarColor = Color(0xFF5C6BC0),
    ),
    SpeakerUi(
        name = "Bob Smith",
        role = "VP Eng",
        company = "SoftSys",
        topics = listOf("Keynote", "Engineering"),
        isFeatured = true,
        featuredCategory = "Keynote",
        avatarColor = Color(0xFF1565C0),
    ),
    SpeakerUi(
        name = "Charlie Brown",
        role = "Founder",
        company = "StartupX",
        topics = listOf("Keynote"),
        isFeatured = true,
        featuredCategory = "Keynote",
        avatarColor = Color(0xFF283593),
    ),
    SpeakerUi(
        name = "David Miller",
        role = "Product Designer",
        company = "CreativeFlow",
        topics = listOf("UX/UI", "Design Systems"),
        avatarColor = Color(0xFF2E7D32),
    ),
    SpeakerUi(
        name = "Elena Rodriguez",
        role = "Senior AI Researcher",
        company = "OpenAI",
        topics = listOf("Machine Learning"),
        avatarColor = Color(0xFF880E4F),
    ),
    SpeakerUi(
        name = "Frank Wright",
        role = "DevOps Lead",
        company = "CloudScale",
        topics = listOf("Kubernetes", "AWS"),
        avatarColor = Color(0xFFE65100),
    ),
    SpeakerUi(
        name = "Grace Hopper",
        role = "Software Architect",
        company = "LegacySys",
        topics = listOf("Architecture"),
        avatarColor = Color(0xFF6A1B9A),
    ),
    SpeakerUi(
        name = "Henry Ford",
        role = "Automotive Engineer",
        company = "AutoMotive",
        topics = listOf("Manufacturing"),
        avatarColor = Color(0xFF37474F),
    ),
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class SpeakersUiState(
    val searchQuery: String = "",
    val allSpeakers: List<SpeakerUi> = defaultSpeakers,
    val bookmarkedNames: Set<String> = defaultSpeakers.filter { it.isBookmarked }.map { it.name }.toSet(),
) {
    val featuredSpeakers: List<SpeakerUi>
        get() = allSpeakers.filter { it.isFeatured }

    val filteredSpeakers: List<SpeakerUi>
        get() = allSpeakers.filter { !it.isFeatured }.filter { speaker ->
            searchQuery.isBlank() ||
                speaker.name.contains(searchQuery, ignoreCase = true) ||
                speaker.topics.any { it.contains(searchQuery, ignoreCase = true) }
        }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class SpeakersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SpeakersUiState())
    val uiState: StateFlow<SpeakersUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleBookmark(name: String) {
        _uiState.update { state ->
            val updated = state.bookmarkedNames.toMutableSet()
            if (!updated.add(name)) updated.remove(name)
            state.copy(bookmarkedNames = updated)
        }
    }
}
