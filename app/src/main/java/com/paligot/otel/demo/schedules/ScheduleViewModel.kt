package com.paligot.otel.demo.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paligot.otel.demo.BuildConfig
import com.paligot.otel.demo.data.AgendaRepository
import com.paligot.otel.demo.data.models.AgendaCategory
import com.paligot.otel.demo.data.models.AgendaFormat
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

// ── UI state ──────────────────────────────────────────────────────────────────

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val month: String = "",
    val selectedDate: String = "",
    val calendarDays: List<CalendarDay> = emptyList(),
    val allTracks: List<String> = listOf("All Tracks") + TrackCategory.entries.map { it.label },
    val selectedTrack: String = "All Tracks",
    val timeSlots: List<TimeSlotUi> = emptyList(),
    val favoriteTitles: Set<String> = emptySet(),
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

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val openTelemetry: OpenTelemetry,
    private val repository: AgendaRepository,
) : ViewModel() {

    private val tracer = openTelemetry.getTracer("schedule-viewmodel")

    private var cachedAgenda: AgendaResponse? = null

    private val _uiState = MutableStateFlow(ScheduleUiState(isLoading = true))
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadAgenda(BuildConfig.EVENT_ID)
    }

    private fun loadAgenda(eventSlug: String) {
        viewModelScope.launch {
            tracer.suspendSpan(
                name = "schedule.load",
                attributes = Attributes.of(
                    AttributeKey.stringKey("event.slug"), eventSlug,
                ),
            ) {
                val agenda = repository.agenda(eventSlug)
                cachedAgenda = agenda
                val dates = agenda.schedules.map { it.date }.distinct().sorted()
                val firstDate = dates.firstOrNull() ?: return@suspendSpan
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        calendarDays = buildCalendarDays(dates, firstDate),
                        selectedDate = firstDate,
                        month = formatMonth(firstDate),
                        timeSlots = buildTimeSlots(agenda, firstDate),
                    )
                }
            }
        }
    }

    fun selectDate(date: String) {
        if (date.isBlank()) return
        val agenda = cachedAgenda ?: return

        tracer.span(
            name = "schedule.selectDate",
            attributes = Attributes.of(
                AttributeKey.stringKey("schedule.date"), date,
            ),
        ) {
            val dates = agenda.schedules.map { it.date }.distinct().sorted()
            _uiState.update { state ->
                state.copy(
                    selectedDate = date,
                    calendarDays = buildCalendarDays(dates, date),
                    month = formatMonth(date),
                    timeSlots = buildTimeSlots(agenda, date),
                    selectedTrack = "All Tracks",
                )
            }
        }
    }

    fun selectTrack(track: String) {
        tracer.span(
            name = "schedule.selectTrack",
            attributes = Attributes.of(
                AttributeKey.stringKey("schedule.track"), track,
            ),
        ) {
            _uiState.update { it.copy(selectedTrack = track) }
        }
    }

    fun toggleFavorite(sessionTitle: String) {
        tracer.span(
            name = "schedule.toggleFavorite",
            attributes = Attributes.of(
                AttributeKey.stringKey("session.title"), sessionTitle,
            ),
        ) {
            _uiState.update { state ->
                val updated = state.favoriteTitles.toMutableSet()
                if (!updated.add(sessionTitle)) updated.remove(sessionTitle)
                state.copy(favoriteTitles = updated)
            }
        }
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private fun buildCalendarDays(dates: List<String>, selectedDate: String): List<CalendarDay> =
        dates.map { date ->
            val ld = LocalDate.parse(date)
            CalendarDay(
                dayAbbr = ld.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(3),
                dayNumber = ld.dayOfMonth,
                isSelected = date == selectedDate,
                date = date,
            )
        }

    private fun formatMonth(date: String): String = try {
        val ld = LocalDate.parse(date)
        "${ld.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${ld.year}"
    } catch (e: Exception) {
        ""
    }

    private fun buildTimeSlots(agenda: AgendaResponse, date: String): List<TimeSlotUi> {
        val sessionById = agenda.sessions.associateBy { it.id }
        val speakerById = agenda.speakers.associateBy { it.id }

        return agenda.schedules
            .filter { it.date == date }
            .sortedWith(compareBy({ it.startTime }, { it.order }))
            .groupBy { it.startTime }
            .entries
            .sortedBy { it.key }
            .mapNotNull { (startTime, slots) ->
                val talkSessions = slots.mapNotNull { slot ->
                    val session = sessionById[slot.sessionId] ?: return@mapNotNull null
                    if (session.type != "talk-session") return@mapNotNull null
                    SessionUi(
                        title = session.title,
                        abstract = session.abstract,
                        room = slot.room,
                        duration = formatDuration(session.formatId, agenda.formats),
                        category = mapCategory(session.categoryId, agenda.categories),
                        speakers = session.speakers.mapNotNull { speakerId ->
                            val speaker = speakerById[speakerId] ?: return@mapNotNull null
                            SpeakerSummaryUi(
                                name = speaker.displayName,
                                photoUrl = speaker.photoUrl.orEmpty(),
                            )
                        },
                        isFavorite = false,
                    )
                }

                if (talkSessions.isNotEmpty()) {
                    TimeSlotUi(time = formatTime(startTime), sessions = talkSessions)
                } else {
                    val eventSession = slots
                        .mapNotNull { sessionById[it.sessionId] }
                        .firstOrNull { it.type == "event-session" }
                        ?: return@mapNotNull null
                    TimeSlotUi(
                        time = formatTime(startTime),
                        isBreak = true,
                        breakLabel = eventSession.title,
                        breakDuration = computeDuration(
                            slots.first().startTime,
                            slots.first().endTime,
                        ),
                    )
                }
            }
    }

    private fun mapCategory(categoryId: String, categories: List<AgendaCategory>): TrackCategory {
        val name = categories.find { it.id == categoryId }?.name ?: return TrackCategory.BACKEND
        return when {
            name.equals("Keynote", ignoreCase = true) -> TrackCategory.KEYNOTE
            name.contains("Web", ignoreCase = true) -> TrackCategory.FRONTEND
            name.contains("AI", ignoreCase = true) || name.contains("Data", ignoreCase = true) ->
                TrackCategory.AI_ML
            else -> TrackCategory.BACKEND
        }
    }

    private fun formatDuration(formatId: String, formats: List<AgendaFormat>): String {
        val time = formats.find { it.id == formatId }?.time ?: 0
        return if (time > 0) "$time min" else ""
    }

    private fun computeDuration(startIso: String, endIso: String): String = try {
        val s = LocalTime.parse(startIso.substringAfter("T").take(8))
        val e = LocalTime.parse(endIso.substringAfter("T").take(8))
        val mins = java.time.Duration.between(s, e).toMinutes()
        "$mins min"
    } catch (ex: Exception) {
        ""
    }

    private fun formatTime(dateTimeStr: String): String = try {
        val time = LocalTime.parse(dateTimeStr.substringAfter("T").take(8))
        val hour = time.hour
        val min = time.minute
        val amPm = if (hour < 12) "AM" else "PM"
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        String.format("%02d:%02d %s", h12, min, amPm)
    } catch (e: Exception) {
        dateTimeStr
    }
}
