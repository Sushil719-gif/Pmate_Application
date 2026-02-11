package com.example.pmate.ui.Student.studentapplications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.Firestore.DataModels.OADetails
import com.example.pmate.Firestore.DataModels.StudentApplicationUI
import kotlinx.coroutines.launch

@Composable
fun StudentApplicationsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    val scope = rememberCoroutineScope()
    val session = LocalSessionManager.current
    val repo = remember { FirestoreRepository(session) }

    var visible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var applications by remember { mutableStateOf<List<StudentApplicationUI>>(emptyList()) }

    LaunchedEffect(Unit) {
        visible = true

        repo.listenStudentApplications { applicantList ->

            scope.launch {

                applications = applicantList
                    .sortedByDescending { it.appliedAt }
                    .mapNotNull { applicant ->

                        val job = repo.getJobById(applicant.jobId)

                        if (job != null) {
                            StudentApplicationUI(
                                companyName = job.company,
                                role = job.role,
                                placementStatus = applicant.status,
                                oaDetails = applicant.oaDetails
                            )
                        } else null
                    }

                loading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(20.dp)
    ) {

        Text("Your Applications", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { 50 }
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
                            company = app.companyName,
                            role = app.role,
                            placementStatus = app.placementStatus,
                            oaDetails = app.oaDetails
                        )
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
    placementStatus: String?,
    oaDetails: OADetails?
) {

    val (statusText, statusColor) = when (placementStatus) {
        "SHORTLISTED" -> "Shortlisted" to Color(0xFFEF6C00)
        "PLACED" -> "Placed" to Color(0xFF2E7D32)
        "NOT_SHORTLISTED" -> "Not Selected" to Color.Red
        else -> "Under Review" to Color(0xFF1565C0)
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            if (oaDetails != null) {

                Spacer(Modifier.height(12.dp))

                Text(
                    text = if (expanded) "Hide OA Details ▲" else "View OA Details ▼",
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { expanded = !expanded }
                )

                AnimatedVisibility(visible = expanded) {

                    Column(
                        Modifier
                            .padding(top = 10.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {

                        Text("OA Venue: ${oaDetails.room}", fontWeight = FontWeight.Bold)
                        Text("Date: ${oaDetails.date}")
                        Text("Time: ${oaDetails.time}")
                        Text("Instructions:", fontWeight = FontWeight.Medium)
                        Text(oaDetails.instructions)
                    }
                }
            }
        }
    }
}
