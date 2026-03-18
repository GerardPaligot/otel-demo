package com.paligot.otel.demo.speakers

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paligot.otel.demo.BuildConfig
import com.paligot.otel.demo.data.AgendaRepository
import com.paligot.otel.demo.data.models.AgendaResponse
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

// ── UI state ──────────────────────────────────────────────────────────────────

data class SpeakersUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val allSpeakers: List<SpeakerUi> = emptyList(),
    val bookmarkedNames: Set<String> = emptySet(),
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

@HiltViewModel
class SpeakersViewModel @Inject constructor(
    private val openTelemetry: OpenTelemetry,
    private val repository: AgendaRepository,
) : ViewModel() {

    private val tracer = openTelemetry.getTracer("speakers-viewmodel")

    private val _uiState = MutableStateFlow(SpeakersUiState())
    val uiState: StateFlow<SpeakersUiState> = _uiState.asStateFlow()

    init {
        loadAgenda(BuildConfig.EVENT_ID)
    }

    private fun loadAgenda(eventSlug: String) {
        viewModelScope.launch {
            tracer.suspendSpan(
                name = "speakers.load",
                attributes = Attributes.of(
                    AttributeKey.stringKey("event.slug"), eventSlug
                ),
            ) {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val agenda = repository.agenda(eventSlug)
                val speakers = mapSpeakers(agenda)
                _uiState.update { it.copy(isLoading = false, allSpeakers = speakers) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        tracer.span(
            name = "speakers.search",
            attributes = Attributes.of(
                AttributeKey.stringKey("search.query"), query,
            ),
        ) {
            _uiState.update { it.copy(searchQuery = query) }
        }
    }

    fun toggleBookmark(name: String) {
        tracer.span(
            name = "speakers.toggleBookmark",
            attributes = Attributes.of(
                AttributeKey.stringKey("speaker.name"), name,
            ),
        ) {
            _uiState.update { state ->
                val updated = state.bookmarkedNames.toMutableSet()
                if (!updated.add(name)) updated.remove(name)
                state.copy(bookmarkedNames = updated)
            }
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private val avatarColors = listOf(
        Color(0xFF5C6BC0), Color(0xFF1565C0), Color(0xFF283593), Color(0xFF2E7D32),
        Color(0xFF880E4F), Color(0xFFE65100), Color(0xFF6A1B9A), Color(0xFF37474F),
        Color(0xFFAD1457), Color(0xFF00695C), Color(0xFF4E342E), Color(0xFF0277BD),
    )

    private fun avatarColor(name: String): Color =
        avatarColors[Math.abs(name.hashCode()) % avatarColors.size]

    private fun mapSpeakers(agenda: AgendaResponse): List<SpeakerUi> {
        val categoryNameById = agenda.categories.associateBy({ it.id }, { it.name })

        val keynoteSessionIds = agenda.sessions
            .filter { s ->
                categoryNameById[s.categoryId]?.equals("Keynote", ignoreCase = true) == true
            }
            .map { it.id }
            .toSet()

        val featuredSpeakerIds = agenda.sessions
            .filter { it.id in keynoteSessionIds }
            .flatMap { it.speakers }
            .toSet()

        val speakerTopics: Map<String, List<String>> = buildMap {
            agenda.sessions.forEach { session ->
                val catName = categoryNameById[session.categoryId] ?: return@forEach
                session.speakers.forEach { speakerId ->
                    @Suppress("UNCHECKED_CAST")
                    (getOrPut(speakerId) { mutableListOf<String>() } as MutableList<String>).add(catName)
                }
            }
        }

        return agenda.speakers.map { s ->
            SpeakerUi(
                name = s.displayName,
                role = s.jobTitle ?: "",
                company = s.company ?: "",
                topics = speakerTopics[s.id]?.distinct() ?: emptyList(),
                isFeatured = s.id in featuredSpeakerIds,
                featuredCategory = if (s.id in featuredSpeakerIds) "Keynote" else "",
                avatarColor = avatarColor(s.displayName),
                photoUrl = s.photoUrl.orEmpty(),
            )
        }
    }
}
