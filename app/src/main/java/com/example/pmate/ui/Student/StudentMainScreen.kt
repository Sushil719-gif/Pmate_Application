package com.example.pmate.ui.Student

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.pmate.ui.Student.studentapplications.StudentApplicationsScreen
import com.example.pmate.ui.Student.studentdashboard.StudentDashboardScreen
import com.example.pmate.ui.Student.studentjobs.StudentJobsListScreen

import com.example.pmate.ui.Student.studentsettings.StudentSettingsScreen


@Composable
fun StudentMainScreen(navController: NavController) {

    var selectedIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Dashboard", "Jobs", "Applications", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Work, Icons.Default.List, Icons.Default.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->

        when (selectedIndex) {
            0 -> StudentDashboardScreen(modifier = Modifier.padding(padding), navController)
            1 -> StudentJobsListScreen(modifier = Modifier.padding(padding), navController)
            2 -> StudentApplicationsScreen(modifier = Modifier.padding(padding), navController)
            3 -> StudentSettingsScreen(modifier = Modifier.padding(padding), navController)
        }
    }
}
