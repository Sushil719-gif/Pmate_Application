package com.example.pmate.ui.Admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pmate.Auth.SessionManager
import com.example.pmate.ui.Admin.dashboard.AdminDashboardScreen
import com.example.pmate.ui.Admin.jobs.AdminJobs
import com.example.pmate.ui.Admin.settings.AdminSettingsScreen
import java.util.Calendar

@Composable
fun AdminMainScreen(navController: NavController) {

    // ▬▬▬ SESSION PROTECTION ▬▬▬
    val context = LocalContext.current
    val session = SessionManager(context)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
    var selectedBatch by rememberSaveable { mutableStateOf(currentYear) }





    LaunchedEffect(Unit) {
        val isLogged = session.isLoggedIn()
        val role = session.getUserRole()

        if (!isLogged || role.lowercase() != "admin") {
            navController.navigate("login/admin") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    // ▬▬▬ END SESSION PROTECTION ▬▬▬

    var selectedIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Dashboard", "Jobs", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Work, Icons.Default.Settings)

    val adminNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            when (index) {
                                0 -> adminNavController.navigate("dashboard")
                                1 -> adminNavController.navigate("jobs")
                                2 -> adminNavController.navigate("settings")
                            }
                        },
                        icon = { Icon(icons[index], contentDescription = null) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = adminNavController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {

            composable("dashboard") {
                AdminDashboardScreen(
                    navController = navController,
                    selectedBatch = selectedBatch,
                    onBatchChange = { selectedBatch = it }
                )
            }

            composable("jobs") {
                AdminJobs(
                    navController = navController,
                    batch = selectedBatch
                )
            }

            composable("settings") {
                AdminSettingsScreen(
                    navController = navController
                )
            }
        }
    }

}
