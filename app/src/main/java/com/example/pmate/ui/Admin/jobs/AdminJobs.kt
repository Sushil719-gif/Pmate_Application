package com.example.pmate.ui.Admin.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun AdminJobs(navController: NavController, modifier: Modifier = Modifier) {

    val repo = remember { FirestoreRepository() }
    var jobList by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            jobList = repo.getAllJobs()
            loading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Title
            Text(
                "All Jobs",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // List Content
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                jobList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Jobs Found", fontSize = 18.sp)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(jobList) { job ->
                            JobListCard(job) {
                                navController.navigate("job_details_admin/${job.jobId}")
                            }
                        }
                    }
                }
            }
        }

        // ----------- Floating Button (Free Floating, Not in container) -----------
        FloatingActionButton(
            onClick = { navController.navigate("addJob") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Job")
        }
    }
}


// Job Card
@Composable
fun JobListCard(job: JobModel, onClick: () -> Unit) {

    var showStatusDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(job.company, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(job.role, fontSize = 16.sp, color = Color.Gray)

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Money, contentDescription = null, tint = Color(0xFF6750A4))
                Text(job.stipend)
            }

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF6750A4))
                Text(job.location)
            }

            Spacer(Modifier.height(8.dp))

            Text("Deadline: ${job.deadline}", fontSize = 14.sp, color = Color.Red)

            Spacer(Modifier.height(8.dp))

            // STATUS LABEL
            Text("Status: ${job.status}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(8.dp))

            // STATUS BUTTON
            OutlinedButton(
                onClick = { showStatusDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Change Status")
            }
        }
    }

    // ---------- STATUS SELECTOR POPUP ----------
    if (showStatusDialog) {
        StatusDialog(job = job, onDismiss = { showStatusDialog = false })
    }
}

@Composable
fun StatusDialog(job: JobModel, onDismiss: () -> Unit) {
    val repo = FirestoreRepository()
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Status") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Button(
                    onClick = {
                        scope.launch {
                            repo.updateJobStatus(job.jobId, "On Hold")
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00))
                ) {
                    Text("On Hold")
                }

                Button(
                    onClick = {
                        scope.launch {
                            repo.updateJobStatus(job.jobId, "Completed")
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Completed")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
