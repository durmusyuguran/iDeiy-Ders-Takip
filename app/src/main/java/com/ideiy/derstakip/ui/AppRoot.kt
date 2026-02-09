package com.ideiy.derstakip.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private sealed class Screen(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
) {
    data object Daily : Screen(
        route = "daily",
        label = "Günlük",
        icon = { Icon(Icons.Default.EditNote, contentDescription = null) }
    )

    data object Stats : Screen(
        route = "stats",
        label = "İstatistik",
        icon = { Icon(Icons.Default.BarChart, contentDescription = null) }
    )

    data object Timetable : Screen(
        route = "timetable",
        label = "Program",
        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
    )
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val items = listOf(Screen.Daily, Screen.Stats, Screen.Timetable)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { s ->
                    NavigationBarItem(
                        selected = currentRoute == s.route,
                        onClick = {
                            nav.navigate(s.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                            }
                        },
                        icon = { s.icon() },
                        label = { Text(s.label) }
                    )
                }
            }
        }
    ) { _ ->
        NavHost(
            navController = nav,
            startDestination = Screen.Daily.route,
            modifier = Modifier
        ) {
            composable(Screen.Daily.route) { DailyEntryScreen() }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Timetable.route) { TimetableScreen() }
        }
    }
}
