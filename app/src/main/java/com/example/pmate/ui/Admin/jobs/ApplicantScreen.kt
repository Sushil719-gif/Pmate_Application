package com.example.pmate.ui.Admin.jobs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.DataModels.Applicant
import com.example.pmate.Firestore.DataModels.StudentModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantsListScreen(
    navController: NavController,
    jobId: String
) {
    val repo = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    var applicants by remember {
        mutableStateOf<List<Pair<String, Pair<Applicant, StudentModel>>>>(emptyList())
    }

    LaunchedEffect(jobId) {
        repo.listenApplicantsForJob(jobId) { updated ->
            applicants = updated
            loading = false
        }
    }

    val underReview = applicants.filter { it.second.first.status == "UNDER_REVIEW" }
    val shortlisted = applicants.filter { it.second.first.status == "SHORTLISTED" }
    val placed = applicants.filter { it.second.first.status == "PLACED" }
    val notSelected = applicants.filter { it.second.first.status == "NOT_SHORTLISTED" }


    val filteredApplicants = when (selectedFilter) {
        "UNDER_REVIEW" -> underReview
        "SHORTLISTED" -> shortlisted
        "PLACED" -> placed
        "NOT_SHORTLISTED" -> notSelected
        else -> applicants
    }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Applicants") })
        }
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (applicants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No applicants yet")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Total Applicants: ${applicants.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    OutlinedButton(onClick = { filterMenuExpanded = true }) {
                        Text("Filter: $selectedFilter")
                    }

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All (${applicants.size})") },
                            onClick = {
                                selectedFilter = "ALL"
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Under Review (${underReview.size})") },
                            onClick = {
                                selectedFilter = "UNDER_REVIEW"
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Shortlisted (${shortlisted.size})") },
                            onClick = {
                                selectedFilter = "SHORTLISTED"
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Placed (${placed.size})") },
                            onClick = {
                                selectedFilter = "PLACED"
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Not Selected (${notSelected.size})") },
                            onClick = {
                                selectedFilter = "NOT_SHORTLISTED"
                                filterMenuExpanded = false
                            }
                        )

                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                if (filteredApplicants.isEmpty()) {
                    item {
                        val message = when (selectedFilter) {
                            "UNDER_REVIEW" -> "No applicants are under review yet."
                            "SHORTLISTED" -> "No applicants have been shortlisted yet."
                            "PLACED" -> "No applicants have been placed yet."
                            "NOT_SHORTLISTED" -> "No applicants have been rejected yet."
                            else -> "No applicants in this section."
                        }


                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = message,
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {

                    items(filteredApplicants) { (applicationId, pair) ->

                        val (applicant, student) = pair
                        val isNew =
                            System.currentTimeMillis() - applicant.appliedAt < 5_000

                        ApplicantCard(
                            student = student,
                            status = applicant.status,
                            isNew = isNew,
                            onUpdateStatus = { newStatus ->
                                scope.launch {
                                    repo.updateApplicationStatus(
                                        applicationId = applicationId,
                                        studentId = applicant.studentId,   // ⭐ VERY IMPORTANT
                                        status = newStatus
                                    )
                                }
                            }

                        )
                    }
                }
            }

        }
    }
}

@Composable
fun ApplicantCard(
    student: StudentModel,
    status: String,
    isNew: Boolean,
    onUpdateStatus: (String) -> Unit
) {
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isNew)
            BorderStroke(2.dp, Color(0xFF4CAF50))
        else null,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(student.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                if (isNew) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "NEW",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(student.email, fontSize = 14.sp, color = Color.Gray)
            Text("Batch: ${student.batchYear}", fontSize = 14.sp)

            Spacer(Modifier.height(8.dp))

            val displayStatus = when (status) {
                "SHORTLISTED" -> "Shortlisted!"
                "PLACED" -> "Placed!"
                "NOT_SHORTLISTED" -> "Not selected!"
                else -> "Under review!"
            }

            Text(
                text = "Status: $displayStatus",
                fontWeight = FontWeight.Medium,
                color = when (status) {
                    "PLACED" -> Color(0xFF2E7D32)
                    "SHORTLISTED" -> Color(0xFFEF6C00)
                    "NOT_SHORTLISTED" -> Color.Red
                    else -> Color(0xFF1565C0)
                }
            )

            Spacer(Modifier.height(12.dp))

            Box {
                OutlinedButton(onClick = { statusMenuExpanded = true }) {
                    Text("Change Status")
                }

                DropdownMenu(
                    expanded = statusMenuExpanded,
                    onDismissRequest = { statusMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Under Review") },
                        onClick = {
                            onUpdateStatus("UNDER_REVIEW")
                            statusMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Shortlisted") },
                        onClick = {
                            onUpdateStatus("SHORTLISTED")
                            statusMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Placed") },
                        onClick = {
                            onUpdateStatus("PLACED")
                            statusMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Not Selected") },
                        onClick = {
                            onUpdateStatus("NOT_SHORTLISTED")
                            statusMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}
