package com.paligot.otel.demo.schedules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.paligot.otel.demo.theme.track_aiml_container
import com.paligot.otel.demo.theme.track_aiml_content
import com.paligot.otel.demo.theme.track_backend_container
import com.paligot.otel.demo.theme.track_backend_content
import com.paligot.otel.demo.theme.track_border_aiml
import com.paligot.otel.demo.theme.track_border_backend
import com.paligot.otel.demo.theme.track_border_frontend
import com.paligot.otel.demo.theme.track_border_keynote
import com.paligot.otel.demo.theme.track_frontend_container
import com.paligot.otel.demo.theme.track_frontend_content
import com.paligot.otel.demo.theme.track_keynote_container
import com.paligot.otel.demo.theme.track_keynote_content

// ── Data models ──────────────────────────────────────────────────────────────

enum class TrackCategory(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
) {
    KEYNOTE(
        label = "Keynote",
        containerColor = track_keynote_container,
        contentColor = track_keynote_content,
        borderColor = track_border_keynote,
    ),
    AI_ML(
        label = "AI & ML",
        containerColor = track_aiml_container,
        contentColor = track_aiml_content,
        borderColor = track_border_aiml,
    ),
    FRONTEND(
        label = "Frontend",
        containerColor = track_frontend_container,
        contentColor = track_frontend_content,
        borderColor = track_border_frontend,
    ),
    BACKEND(
        label = "Backend",
        containerColor = track_backend_container,
        contentColor = track_backend_content,
        borderColor = track_border_backend,
    ),
}

data class SessionUi(
    val title: String,
    val abstract: String,
    val room: String,
    val duration: String,
    val category: TrackCategory,
    val speakers: List<String>,
    val isFavorite: Boolean = false,
)

data class TimeSlotUi(
    val time: String,
    val sessions: List<SessionUi> = emptyList(),
    val isBreak: Boolean = false,
    val breakLabel: String = "",
    val breakDuration: String = "",
)

data class CalendarDay(
    val dayAbbr: String,
    val dayNumber: Int,
    val isSelected: Boolean = false,
)

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
) {
    HOME("Home", Icons.Outlined.Home, "home"),
    SCHEDULE("Schedule", Icons.Outlined.CalendarMonth, "schedule"),
    SPEAKERS("Speakers", Icons.Outlined.Person, "speakers"),
    SPONSORS("Sponsors", Icons.Outlined.Sell, "sponsors"),
}

// ── Sample data ───────────────────────────────────────────────────────────────

// Sample data and UI state are managed by ScheduleViewModel

// ── Composables ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                        )
                    }
                },
            )
        },
        bottomBar = {
            AppBottomBar(
                navController = navController,
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CalendarStrip(
                month = uiState.month,
                days = uiState.calendarDays,
                modifier = Modifier.fillMaxWidth(),
            )
            TrackFilterRow(
                tracks = uiState.allTracks,
                selectedTrack = uiState.selectedTrack,
                onTrackSelected = { viewModel.selectTrack(it) },
                modifier = Modifier.fillMaxWidth(),
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            ) {
                uiState.filteredTimeSlots.forEach { slot ->
                    item {
                        Text(
                            text = slot.time.ifBlank { "" },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                        )
                    }
                    if (slot.isBreak) {
                        item {
                            LunchBreakItem(
                                label = slot.breakLabel,
                                duration = slot.breakDuration,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        items(slot.sessions) { session ->
                            ScheduleItem(
                                title = session.title,
                                abstract = session.abstract,
                                room = session.room,
                                time = session.duration,
                                category = session.category.label,
                                categoryContainerColor = session.category.containerColor,
                                categoryContentColor = session.category.contentColor,
                                borderColor = session.category.borderColor,
                                speakers = session.speakers,
                                isFavorite = uiState.favoriteTitles.contains(session.title),
                                onFavoriteClick = { viewModel.toggleFavorite(session.title) },
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarStrip(
    month: String,
    days: List<CalendarDay>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous week",
                )
            }
            Text(
                text = month,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next week",
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
        ) {
            days.forEach { day ->
                CalendarDayCell(day = day)
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = day.dayAbbr,
            style = MaterialTheme.typography.labelSmall,
            color = if (day.isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (day.isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                )
                .clickable {},
        ) {
            Text(
                text = day.dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (day.isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun TrackFilterRow(
    tracks: List<String>,
    selectedTrack: String,
    onTrackSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        tracks.forEach { track ->
            FilterChip(
                selected = track == selectedTrack,
                onClick = { onTrackSelected(track) },
                label = { Text(track) },
            )
        }
    }
}

@Composable
fun AppBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(modifier = modifier) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
