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
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.Firestore.DataModels.Applicant
import com.example.pmate.Firestore.DataModels.StudentApplicationUI
import kotlinx.coroutines.launch

@Composable
fun StudentApplicationsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    val scope = rememberCoroutineScope()

    val repo = remember { FirestoreRepository() }

    var visible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var applications by remember { mutableStateOf<List<StudentApplicationUI>>(emptyList()) }

    LaunchedEffect(Unit) {
        visible = true

        repo.listenStudentApplications { applicantList ->

            scope.launch {

                applications = applicantList.mapNotNull { applicant ->

                    val job = repo.getJobById(applicant.jobId)
                    val student = repo.getStudentById(applicant.studentId)

                    if (job != null && student != null) {
                        StudentApplicationUI(
                            companyName = job.company,
                            role = job.role,
                            placementStatus = applicant.status

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
                            placementStatus = app.placementStatus
                        ) { }
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
    onClick: () -> Unit
) {

    val (statusText, statusColor) = when (placementStatus) {
        "SHORTLISTED" -> "Shortlisted" to Color(0xFFEF6C00)
        "PLACED" -> "Placed" to Color(0xFF2E7D32)
        "NOT_SHORTLISTED" -> "Not Selected" to Color.Red
        else -> "Under Review" to Color(0xFF1565C0)
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
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
