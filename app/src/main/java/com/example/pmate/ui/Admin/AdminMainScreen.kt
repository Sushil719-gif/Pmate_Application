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
import com.example.pmate.Auth.SessionManager

import kotlinx.coroutines.launch

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
            0 -> AdminDashboardScreen(
                navController = navController,
                selectedBatch = selectedBatch,
                onBatchChange = { selectedBatch = it }
            )

            1 -> AdminJobs(
                navController = navController,
                batch = selectedBatch,
                modifier = Modifier.padding(padding)
            )
            2 -> AdminSettingsScreen(
                modifier = Modifier.padding(padding),
                navController = navController
            )
        }

    }
}
