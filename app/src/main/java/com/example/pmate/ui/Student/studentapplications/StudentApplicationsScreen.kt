package com.example.pmate.ui.Student.studentapplications





import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Temporary static model until Firestore is added
data class DummyApplication(
    val company: String,
    val role: String,
    val status: String,
    val jobId: String
)

@Composable
fun StudentApplicationsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // ------------------------------------------------------------
    // STATIC APPLICATION LIST (will replace with Firestore later)
    // ------------------------------------------------------------
    val applications = listOf(
        DummyApplication("Google", "Android Intern", "Under Review", "1"),
        DummyApplication("TCS", "Software Engineer", "Shortlisted", "2"),
        DummyApplication("Infosys", "Support Exec", "Rejected", "3"),
        DummyApplication("Wipro", "Cloud Analyst", "Selected", "4")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(20.dp)
    ) {

        Text("Your Applications", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
        ) {

            if (applications.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("You have not applied for any job yet.", color = Color.Gray)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    applications.forEach { app ->

                        ApplicationTile(
                            company = app.company,
                            role = app.role,
                            status = app.status
                        ) {
                            // Navigate to job details if needed later
                            // navController.navigate("job_details/${app.jobId}")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ApplicationTile(
    company: String,
    role: String,
    status: String,
    onClick: () -> Unit
) {

    val statusColor = when (status) {
        "Shortlisted" -> Color(0xFF2E7D32)
        "Under Review" -> Color(0xFF0277BD)
        "Selected" -> Color(0xFF4CAF50)
        "Rejected" -> Color.Red
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(Modifier.padding(16.dp)) {

            Text(company, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(role, fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Status:", fontWeight = FontWeight.Medium)
                Text(
                    text = status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
