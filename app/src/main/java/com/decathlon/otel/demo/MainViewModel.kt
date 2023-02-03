package com.decathlon.otel.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.decathlon.otel.demo.data.ConferenceApi
import com.decathlon.otel.demo.data.EventRepository
import com.decathlon.otel.demo.data.models.EventItemList
import kotlinx.coroutines.flow.*

sealed class EventListUiState {
    object Loading : EventListUiState()
    data class Success(val events: List<EventItemList>) : EventListUiState()
    data class Failure(val throwable: Throwable) : EventListUiState()
}

class MainViewModel(private val repository: EventRepository) : ViewModel() {
    val uiState: StateFlow<EventListUiState> = flow { emit(repository.events()) }
        .map { EventListUiState.Success(it) as EventListUiState }
        .catch { emit(EventListUiState.Failure(it)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = EventListUiState.Loading
        )

    object Factory {
        fun create(repository: EventRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MainViewModel(repository = repository) as T
        }
    }
}
