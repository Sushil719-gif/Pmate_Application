package com.example.pmate.ui.Student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.Scrolls.VerticalScrollableScreen
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Description


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    jobId: String,
    isAdmin: Boolean = false
) {

    val repo = remember { FirestoreRepository() }
    var job by remember { mutableStateOf<JobModel?>(null) }
    var loading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    //  Fetch job from Firestore
    LaunchedEffect(jobId) {
        scope.launch {
            job = repo.getJobById(jobId)
            loading = false
        }
    }

    //  Loading Screen
    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    //  Job Not Found
    if (job == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Job Not Found", color = Color.Red, fontSize = 18.sp)
        }
        return
    }

    val visible by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Job Details") }) }
    ) { padding ->

        VerticalScrollableScreen(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            val currentJob = job!!

            //  HEADER
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
            ) {
                JobHeader(currentJob.company, currentJob.role)
            }

            //  STATS
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 60 })
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        DetailChip(Icons.Default.Money, currentJob.stipend)
                    }
                    Box(Modifier.weight(1f)) {
                        DetailChip(Icons.Default.LocationOn, currentJob.location)
                    }
                }
            }

            //  Description
            DetailSection("Job Description", currentJob.description)

            //  Skills
            if (currentJob.skills.isNotEmpty()) {
                DetailSection(
                    "Required Skills",
                    currentJob.skills.joinToString(", ")
                )
            }

            //  Instructions Section (Expandable)
            if (!currentJob.instructions.isNullOrBlank()) {

                var expanded by remember { mutableStateOf(false) }

                Text(
                    "Instructions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { expanded = !expanded }
                            .padding(16.dp)
                    ) {

                        Text(
                            if (expanded) "Hide Instructions ▲" else "Show Instructions ▼",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5E3BBF)
                        )

                        AnimatedVisibility(expanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                // Convert to bullet points automatically
                                currentJob.instructions
                                    .split("\n")
                                    .filter { it.isNotBlank() }
                                    .forEach { line ->
                                        Row {
                                            Text("•  ", fontSize = 15.sp)
                                            Text(line.trim(), fontSize = 15.sp)
                                        }
                                    }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }



            // ⭐ Job Files Section
            if (currentJob.files.isNotEmpty()) {
                Text(
                    "Job Files",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currentJob.files.forEachIndexed { index, url ->
                        FileItemCard(
                            fileName = "File ${index + 1}",
                            fileUrl = url
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }


            // =======================================================================
            // ⭐ Apply button with real Firestore apply logic
            // =======================================================================
            if (!isAdmin) {

                var applyLoading by remember { mutableStateOf(false) }
                var applied by remember { mutableStateOf(false) }
                var feedbackMessage by remember { mutableStateOf("") }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // Show success/failure message
                    if (feedbackMessage.isNotEmpty()) {
                        Text(
                            feedbackMessage,
                            color = if (applied) Color(0xFF2E7D32) else Color.Red,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = {
                            applyLoading = true

                            // ⭐ Replace with actual logged-in student ID later
                            val studentId = "TEMP_STUDENT_ID"

                            scope.launch {
                                val success = repo.applyForJob(jobId, studentId)
                                applyLoading = false

                                if (success) {
                                    applied = true
                                    feedbackMessage = "Successfully Applied!"
                                } else {
                                    applied = false
                                    feedbackMessage = "Failed to Apply. Try Again!"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !applied && !applyLoading
                    ) {
                        if (applyLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (applied) "Applied" else "Apply Now",
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// =======================================================================
// Helper UI Components
// =======================================================================

@Composable
fun JobHeader(company: String, role: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
            )

            Spacer(Modifier.height(16.dp))

            Text(company, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(role, fontSize = 16.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DetailChip(icon: ImageVector, title: String) {
    Card(
        modifier = Modifier.height(70.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6750A4))
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 14.sp)
        }
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    Column {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Text(
                content,
                modifier = Modifier.padding(16.dp),
                fontSize = 15.sp
            )
        }
    }
}
//File Item Card
@Composable
fun FileItemCard(fileName: String, fileUrl: String) {

    val context = LocalContext.current   // <-- FIXED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)    // <-- use context here
            },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val icon = when {
                fileUrl.endsWith(".pdf") -> Icons.Default.PictureAsPdf
                fileUrl.endsWith(".jpg") || fileUrl.endsWith(".png") -> Icons.Default.Image
                else -> Icons.Default.Description
            }

            Icon(icon, contentDescription = null, tint = Color(0xFF6750A4))

            Spacer(Modifier.width(16.dp))

            Text(
                text = fileName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
