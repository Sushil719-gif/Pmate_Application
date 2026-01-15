package com.example.pmate.ui.Admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

import com.example.pmate.ui.Admin.dashboard.AdminDashboardScreen
import com.example.pmate.ui.Admin.jobs.AdminJobs
import com.example.pmate.ui.Admin.settings.AdminSettingsScreen

@Composable
fun AdminMainScreen(navController: NavController) {

    var selectedIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Dashboard", "Jobs", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Work, Icons.Default.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(icons[index], contentDescription = null) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->

        when (selectedIndex) {
            0 -> AdminDashboardScreen(navController)
            1 -> AdminJobs(navController = navController, modifier = Modifier.padding(padding))
            2 -> AdminSettingsScreen(modifier = Modifier.padding(padding), navController = navController)
        }
    }
}


