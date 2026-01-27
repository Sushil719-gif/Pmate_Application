package com.example.pmate.ui.Student.studentjobs



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun StudentJobsListScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val repo = remember { FirestoreRepository() }
    var jobs by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val student = repo.getCurrentStudent()
        val studentBatch = student.batchYear

        jobs = repo.getAllJobs()
            .filter { it.batchYear == studentBatch }

        loading = false
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Available Jobs",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 60 })
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Highlight Card (optional)
                HighlightCard(
                    title = "Upcoming Events!",
                    subtitle = "Check your notices",
                    icon = Icons.Default.Event
                )

                if (loading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (jobs.isEmpty()) {
                        Text(
                            "No jobs available right now.",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    } else {
                        jobs.forEach { job ->
                            JobTile(
                                job = job,
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
}

@Composable
fun JobTile(
    job: JobModel,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(20.dp)
    )
    {

        Column(modifier = Modifier.padding(18.dp)) {

            // ---------- Top Row ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        job.company,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        job.role,
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }

                // Batch Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFEEE5FF)
                ) {
                    Text(
                        text = "${job.batchYear} Batch",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5E3BBF)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Divider()

            Spacer(Modifier.height(14.dp))

            // ---------- Bottom Row ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "💰 ${job.stipend}",
                    fontSize = 14.sp
                )

                Text(
                    "📍 ${job.location}",
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
fun HighlightCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
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
