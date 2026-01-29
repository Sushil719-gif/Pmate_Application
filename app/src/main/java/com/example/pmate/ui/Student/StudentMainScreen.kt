package com.example.pmate.ui.Student

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.pmate.Auth.SessionManager
import androidx.navigation.compose.*




import com.example.pmate.ui.Student.studentapplications.StudentApplicationsScreen
import com.example.pmate.ui.Student.studentdashboard.StudentDashboardScreen
import com.example.pmate.ui.Student.studentjobs.StudentJobsListScreen
import com.example.pmate.ui.Student.studentsettings.StudentSettingsScreen

@Composable
fun StudentMainScreen(navController: NavController) {

    val context = LocalContext.current
    val session = SessionManager(context)

    LaunchedEffect(Unit) {
        val isLogged = session.isLoggedIn()
        val role = session.getUserRole()

        if (!isLogged || role.lowercase() != "student") {
            navController.navigate("login/student") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val studentNavController = rememberNavController()
    var selectedIndex by remember { mutableStateOf(0) }

    val tabs = listOf("dashboard", "jobs", "applications", "settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Work, Icons.Default.List, Icons.Default.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, route ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            studentNavController.navigate(route)
                        },
                        icon = { Icon(icons[index], null) },
                        label = { Text(route.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = studentNavController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {

            composable("dashboard") {
                StudentDashboardScreen(navController = navController)
            }

            composable("jobs") {
                StudentJobsListScreen(navController = navController)
            }

            composable("applications") {
                StudentApplicationsScreen(navController = navController)
            }

            composable("settings") {
                StudentSettingsScreen(navController = navController)
            }
        }
    }
}
