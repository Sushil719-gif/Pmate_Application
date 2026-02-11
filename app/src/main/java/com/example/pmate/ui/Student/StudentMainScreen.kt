package com.example.pmate.ui.Student

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.pmate.Auth.SessionManager
import androidx.navigation.compose.*
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.Auth.StudentProfileSetupScreen
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository


import com.example.pmate.ui.Student.studentapplications.StudentApplicationsScreen
import com.example.pmate.ui.Student.studentdashboard.StudentDashboardScreen
import com.example.pmate.ui.Student.studentjobs.StudentJobsListScreen
import com.example.pmate.ui.Student.studentsettings.StudentSettingsScreen
import com.example.pmate.viewmodel.StudentViewModel
import com.example.pmate.viewmodel.StudentViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun StudentMainScreen(navController: NavController) {

    val context = LocalContext.current
    val session = LocalSessionManager.current

    val repo = remember { FirestoreRepository(session) }

    val studentViewModel: StudentViewModel = viewModel(
        factory = StudentViewModelFactory(repo)
    )

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        studentViewModel.loadStudent(uid)
    }

    val student by studentViewModel.student.collectAsState()

    //  PROFILE GATE (FIRST PRIORITY)
    if (student != null &&
        (student!!.name.isBlank() ||
                student!!.usn.isBlank() ||
                student!!.branch.isBlank() ||
                student!!.batchYear.isBlank() ||
                student!!.gender.isBlank() ||
                student!!.phone.isBlank())
    ) {
        StudentProfileSetupScreen(repo, student!!) {
            studentViewModel.loadStudent(
                FirebaseAuth.getInstance().currentUser!!.uid
            )
        }
        return
    }

    //  LOGIN CHECK
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
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Work,
        Icons.Default.List,
        Icons.Default.Settings
    )

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
