package com.decathlon.otel.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.decathlon.otel.demo.data.ConferenceApi
import com.decathlon.otel.demo.data.EventRepository
import com.decathlon.otel.demo.ui.theme.OteldemoTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val telemetry = (application as MainApplication).openTelemetry
        val baseUrl = "https://cms4partners-ce427.nw.r.appspot.com"
        val api = ConferenceApi.create(telemetry, baseUrl, true)
        val repository = EventRepository(telemetry, api)
        setContent {
            OteldemoTheme {
                val viewModel = viewModel<MainViewModel>(
                    factory = MainViewModel.Factory.create(repository)
                )
                Scaffold {
                    val uiState = viewModel.uiState.collectAsState()
                    when (uiState.value) {
                        is EventListUiState.Loading -> Text("Loading")
                        is EventListUiState.Failure -> Text("Error")
                        is EventListUiState.Success -> {
                            LazyColumn(contentPadding = it) {
                                items((uiState.value as EventListUiState.Success).events) {
                                    ListItem {
                                        Text(it.name)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
