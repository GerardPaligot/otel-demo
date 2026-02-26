package com.paligot.otel.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paligot.otel.demo.data.ConferenceApi
import com.paligot.otel.demo.data.EventRepository
import com.paligot.otel.demo.events.EventPane
import com.paligot.otel.demo.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val telemetry = (application as MainApplication).openTelemetry
        val baseUrl = "https://cms4partners-ce427.nw.r.appspot.com"
        val api = ConferenceApi.Companion.create(telemetry, baseUrl, true)
        val repository = EventRepository(telemetry, api)
        setContent {
            val viewModel = viewModel<MainViewModel>(
                factory = MainViewModel.Factory.create(repository)
            )
            AppTheme {
                EventPane(viewModel)
            }
        }
    }
}
