package com.decathlon.otel.demo.events

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.decathlon.otel.demo.EventListUiState
import com.decathlon.otel.demo.MainViewModel

@Composable
fun EventPane(
    viewModel: MainViewModel
) {
    Scaffold { contentPadding ->
        val uiState = viewModel.uiState.collectAsState()
        when (uiState.value) {
            is EventListUiState.Loading -> Text("Loading")
            is EventListUiState.Failure -> Text("Error")
            is EventListUiState.Success -> {
                LazyColumn(contentPadding = contentPadding) {
                    items((uiState.value as EventListUiState.Success).events) {
                        ListItem(
                            headlineContent = { Text(it.name) }
                        )
                    }
                }
            }
        }
    }
}