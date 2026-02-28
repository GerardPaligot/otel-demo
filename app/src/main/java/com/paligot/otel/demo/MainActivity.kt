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
        setContent {
            AppTheme {
                EventPane()
            }
        }
    }
}
