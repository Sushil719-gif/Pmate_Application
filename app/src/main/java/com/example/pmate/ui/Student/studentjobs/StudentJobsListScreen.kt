package com.example.pmate.ui.Student.studentjobs



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.Auth.SessionManager
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

    val context = LocalContext.current
    val session = LocalSessionManager.current
    val scope = rememberCoroutineScope()

    val repo = remember { FirestoreRepository(session) }

    var jobs by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var lastVisit by remember { mutableStateOf(0L) }



    LaunchedEffect(Unit) {
        loading = true

        val student = repo.getCurrentStudent()
        val studentBatch = student.batchYear

        // 1⃣ Read old visit time ONCE
        lastVisit = session.getLastJobsVisit()

        repo.listenAllJobs { allJobs ->
            jobs = allJobs.filter { job ->
                job.batchYear == studentBatch && job.active
            }
            loading = false
        }
    }

    LaunchedEffect(jobs) {
        if (jobs.isNotEmpty()) {
            session.setLastJobsVisit(System.currentTimeMillis())
        }
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

                            val isNew = job.createdAt > lastVisit


                            JobTile(
                                job = job,
                                isNew = isNew,
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
    isNew: Boolean,
    onClick: () -> Unit
)
 {

     Card(
         modifier = Modifier
             .fillMaxWidth()
             .clickable { onClick() },
         border = if (isNew)
             BorderStroke(2.dp, Color(0xFF4CAF50))
         else null,
         elevation = CardDefaults.cardElevation(8.dp),
         shape = RoundedCornerShape(20.dp)
     )

     {
        Column(modifier = Modifier.padding(18.dp)) {

            // ---------- Top ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Text(job.company, fontSize = 19.sp, fontWeight = FontWeight.Bold)

                        if (isNew) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "NEW",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(job.role, fontSize = 15.sp, color = Color.Gray)
                }


                // Batch Badge (keep highlight)
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

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // ---------- Branches ----------
            Text(
                text = "Eligible Branches",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = job.branches.joinToString(", "),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5E3BBF)
            )

            Spacer(Modifier.height(10.dp))

            // ---------- Deadline (keep red) ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏰ Deadline: ", fontSize = 13.sp, color = Color.Gray)
                Text(
                    job.deadline,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Red
                )
            }

            Spacer(Modifier.height(10.dp))

            // ---------- Bottom ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("💰 ${job.stipend}", fontSize = 14.sp)
                Text("📍 ${job.location}", fontSize = 14.sp)
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
