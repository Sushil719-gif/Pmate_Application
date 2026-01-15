package com.example.pmate.ui.Student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.Scrolls.VerticalScrollableScreen


// ---------------------------------------------------------
// UPDATED: StudentHomeScreen NOW RECEIVES navController
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(navController: NavController) {

    val items = listOf("Home", "Applications", "Profile")
    val icons = listOf(Icons.Default.Home, Icons.Default.Assignment, Icons.Default.Person)

    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(items[selectedIndex]) }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(icons[index], contentDescription = null) },
                        label = { Text(item) }
                    )
                }
            }
        }
    ) { padding ->

        when (selectedIndex) {
            0 -> StudentDashboard(
                modifier = Modifier.padding(padding),
                navController = navController
            )
            1 -> StudentApplications(modifier = Modifier.padding(padding))
            2 -> StudentProfile(modifier = Modifier.padding(padding))
        }
    }
}



// ---------------------------------------------------------
// STUDENT DASHBOARD (UPDATED WITH navController)
// ---------------------------------------------------------
@Composable
fun StudentDashboard(modifier: Modifier = Modifier, navController: NavController) {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // 🔹 Firestore Repository
    val repo = remember { FirestoreRepository() }

    // 🔹 Job State
    var jobs by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // 🔹 Fetch jobs once
    LaunchedEffect(Unit) {
        jobs = repo.getAllJobs()
        loading = false
    }

    VerticalScrollableScreen(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(20.dp)
    ) {

        Text(
            text = "Welcome Student 👋",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                HighlightCard(
                    title = "Upcoming Interview",
                    subtitle = "Wipro · 12th Jan · 10:00 AM",
                    icon = Icons.Default.Event
                )

                // ------------------------------------------------------------------
                //      🔥 SHOW REAL JOBS FROM FIRESTORE
                // ------------------------------------------------------------------
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    jobs.forEach { job ->
                        JobRecommendationCard(
                            company = job.company,
                            role = job.role,
                            stipend = job.stipend,
                            location = job.location,
                            onClick = {
                                navController.navigate("job_details/${job.jobId}")
                            }
                        )
                    }
                }
            }
        }
    }
}



// ---------------------------------------------------------
// HIGHLIGHT CARD
// ---------------------------------------------------------
@Composable
fun HighlightCard(title: String, subtitle: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEE5FF)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF5E3BBF), modifier = Modifier.size(40.dp))

            Spacer(Modifier.width(16.dp))

            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}



// ---------------------------------------------------------
// UPDATED JobRecommendationCard (with onClick)
// ---------------------------------------------------------
@Composable
fun JobRecommendationCard(
    company: String,
    role: String,
    stipend: String,
    location: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(company, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(role, fontSize = 15.sp, color = Color.Gray)

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Stipend: $stipend", fontSize = 14.sp)
                Text("Location: $location", fontSize = 14.sp)
            }
        }
    }
}



// ---------------------------------------------------------
// APPLICATIONS SCREEN
// ---------------------------------------------------------
@Composable
fun StudentApplications(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Your Applications", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        ApplicationCard("Google", "Android Intern", "Under Review")
        ApplicationCard("TCS", "Software Engineer", "Shortlisted")
        ApplicationCard("Infosys", "Support Engineer", "Rejected")
    }
}


// ---------------------------------------------------------
// APPLICATION CARD
// ---------------------------------------------------------
@Composable
fun ApplicationCard(company: String, role: String, status: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(company, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(role, fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Status: $status",
                color = when (status) {
                    "Shortlisted" -> Color(0xFF2E7D32)
                    "Under Review" -> Color(0xFF0277BD)
                    else -> Color.Red
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}



// ---------------------------------------------------------
// PROFILE
// ---------------------------------------------------------
@Composable
fun StudentProfile(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
            )

            Spacer(Modifier.width(20.dp))

            Column {
                Text("Student Name", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("student@college.edu", color = Color.Gray)
            }
        }

        Spacer(Modifier.height(30.dp))

        ProfileOption("Edit Profile", Icons.Default.Edit)
        ProfileOption("Change Password", Icons.Default.Lock)
        ProfileOption("Notifications", Icons.Default.Notifications)
        ProfileOption("Logout", Icons.Default.Logout)
    }
}


// ---------------------------------------------------------
@Composable
fun ProfileOption(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 16.sp)
    }
}
