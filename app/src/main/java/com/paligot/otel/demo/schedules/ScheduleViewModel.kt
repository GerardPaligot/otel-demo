package com.paligot.otel.demo.schedules

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── Sample data ───────────────────────────────────────────────────────────────

private val defaultCalendarDays = listOf(
    CalendarDay("Mon", 2),
    CalendarDay("Tue", 3),
    CalendarDay("Wed", 4),
    CalendarDay("Thu", 5, isSelected = true),
    CalendarDay("Fri", 6),
    CalendarDay("Sat", 7),
    CalendarDay("Sun", 8),
)

private val defaultTimeSlots = listOf(
    TimeSlotUi(
        time = "09:00 AM",
        sessions = listOf(
            SessionUi(
                title = "The Future of Web Development in 2024",
                abstract = "An inspiring kickoff exploring the next generation of web technologies, frameworks,...",
                room = "Main Hall A",
                duration = "60 min",
                category = TrackCategory.KEYNOTE,
                speakers = listOf("Alex Johnson", "Maria Chen"),
                isFavorite = false,
            )
        )
    ),
    TimeSlotUi(
        time = "10:15 AM",
        sessions = listOf(
            SessionUi(
                title = "Building Resilient LLM Applications",
                abstract = "Learn strategies for prompt engineering, managing context windows, and ensuring...",
                room = "Room 204",
                duration = "45 min",
                category = TrackCategory.AI_ML,
                speakers = listOf("Sarah Jenkins"),
                isFavorite = true,
            ),
            SessionUi(
                title = "State Management at Scale",
                abstract = "Comparing Redux, Zustand, and Context API for large scale enterprise applications.",
                room = "Room 101",
                duration = "45 min",
                category = TrackCategory.FRONTEND,
                speakers = listOf("David Chen"),
                isFavorite = false,
            ),
        )
    ),
    TimeSlotUi(
        time = "11:15 AM",
        sessions = listOf(
            SessionUi(
                title = "Microservices vs Monolith: The Eternal Debate",
                abstract = "When to break up your monolith and when to keep it together. Real world case studies.",
                room = "Auditorium B",
                duration = "45 min",
                category = TrackCategory.BACKEND,
                speakers = listOf("Elena Rodriguez"),
                isFavorite = false,
            )
        )
    ),
    TimeSlotUi(
        time = "12:00 PM",
        isBreak = true,
        breakLabel = "Lunch Break",
        breakDuration = "60 min",
    ),
)

private val defaultAllTracks = listOf("All Tracks") + TrackCategory.entries.map { it.label }

private val defaultFavoriteTitles: Set<String> =
    defaultTimeSlots.flatMap { it.sessions }.filter { it.isFavorite }.map { it.title }.toSet()

// ── UI state ──────────────────────────────────────────────────────────────────

data class ScheduleUiState(
    val month: String = "October 2023",
    val calendarDays: List<CalendarDay> = defaultCalendarDays,
    val allTracks: List<String> = defaultAllTracks,
    val selectedTrack: String = "All Tracks",
    val timeSlots: List<TimeSlotUi> = defaultTimeSlots,
    val favoriteTitles: Set<String> = defaultFavoriteTitles,
) {
    val filteredTimeSlots: List<TimeSlotUi>
        get() = if (selectedTrack == "All Tracks") timeSlots
        else timeSlots.filter { slot ->
            slot.isBreak || slot.sessions.any { it.category.label == selectedTrack }
        }.map { slot ->
            if (slot.isBreak) slot
            else slot.copy(sessions = slot.sessions.filter { it.category.label == selectedTrack })
        }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ScheduleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    fun selectTrack(track: String) {
        _uiState.update { it.copy(selectedTrack = track) }
    }

    fun toggleFavorite(sessionTitle: String) {
        _uiState.update { state ->
            val updated = state.favoriteTitles.toMutableSet()
            if (!updated.add(sessionTitle)) updated.remove(sessionTitle)
            state.copy(favoriteTitles = updated)
        }
    }
}
