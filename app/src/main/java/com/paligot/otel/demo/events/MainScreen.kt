package com.paligot.otel.demo.events

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paligot.otel.demo.home.HomeScreen
import com.paligot.otel.demo.schedules.BottomNavItem
import com.paligot.otel.demo.schedules.ScheduleScreen
import com.paligot.otel.demo.speakers.SpeakersScreen
import com.paligot.otel.demo.sponsors.SponsorsScreen

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BottomNavItem.HOME.route,
        modifier = modifier,
    ) {
        composable(BottomNavItem.HOME.route) {
            HomeScreen(navController = navController)
        }
        composable(BottomNavItem.SCHEDULE.route) {
            ScheduleScreen(navController = navController)
        }
        composable(BottomNavItem.SPEAKERS.route) {
            SpeakersScreen(navController = navController)
        }
        composable(BottomNavItem.SPONSORS.route) {
            SponsorsScreen(navController = navController)
        }
    }
}
