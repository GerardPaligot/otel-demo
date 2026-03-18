package com.paligot.otel.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paligot.otel.demo.schedules.BottomNavItem
import com.paligot.otel.demo.schedules.ScheduleScreen
import com.paligot.otel.demo.speakers.SpeakersScreen
import com.paligot.otel.demo.sponsors.SponsorsScreen
import com.paligot.otel.demo.theme.AppTheme
import com.paligot.otel.demo.website.WebsiteScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                DisposableEffect(navController) {
                    val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                        (application as MainApplication).currentScreen =
                            destination.route ?: "unknown"
                    }
                    navController.addOnDestinationChangedListener(listener)
                    onDispose { navController.removeOnDestinationChangedListener(listener) }
                }
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.SCHEDULE.route,
                ) {
                    composable(BottomNavItem.SCHEDULE.route) {
                        ScheduleScreen(navController = navController)
                    }
                    composable(BottomNavItem.SPEAKERS.route) {
                        SpeakersScreen(navController = navController)
                    }
                    composable(BottomNavItem.SPONSORS.route) {
                        SponsorsScreen(navController = navController)
                    }
                    composable(BottomNavItem.WEBSITE.route) {
                        WebsiteScreen(navController = navController)
                    }
                }
            }
        }
    }
}
