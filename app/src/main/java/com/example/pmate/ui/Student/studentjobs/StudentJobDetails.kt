package com.example.pmate.ui.Student.studentjobs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.Scrolls.VerticalScrollableScreen
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.Auth.SessionManager
import com.example.pmate.CommonReusableUIComponents.JobHeader
import com.example.pmate.ui.Student.studentjobs.ApplyLogicFiles.ApplyLogic
import com.example.pmate.ui.Student.studentjobs.ApplyLogicFiles.ApplyState
import com.example.pmate.ui.Student.studentjobs.ApplyLogicFiles.GoogleFormPrefillBuilder
import com.example.pmate.viewmodel.StudentViewModel
import com.example.pmate.viewmodel.StudentViewModelFactory
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    navController: NavController,
    jobId: String,
    isAdmin: Boolean = false
)
 {


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
    var studentChecked by remember { mutableStateOf(false) }


    val context = LocalContext.current

    var job by remember { mutableStateOf<JobModel?>(null) }
    var loading by remember { mutableStateOf(true) }
    var alreadyApplied by remember { mutableStateOf(false) }



    val scope = rememberCoroutineScope()

    //  student data
    val studentBatch = student?.batchYear ?: ""
    val studentStatus = student?.status ?: ""
    val placementStatus = student?.placementStatus ?: ""
    val studentBranch = student?.branch ?: ""



    // Apply states (MOVED UP)
    var applyLoading by remember { mutableStateOf(false) }


    // Fetch job

    LaunchedEffect(jobId) {
        job = repo.getJobById(jobId)
        loading = false
    }

    LaunchedEffect(jobId, student, job) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        if (student != null && job != null) {
            alreadyApplied = repo.hasAlreadyApplied(
                jobId = jobId,
                studentId = uid
            )
        }
    }



    val currentJob = job
    val currentStudent = student

    if (loading || currentJob == null || currentStudent == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val applyState =
        ApplyLogic.getApplyState(currentStudent, currentJob, alreadyApplied)






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

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
            ) {
                JobHeader(
                    company = currentJob.company,
                    role = currentJob.role,
                    batch = currentJob.batchYear,
                    deadline = currentJob.deadline
                )


            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 60 })
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            DetailChip(
                                Icons.Default.Money,
                                "${currentJob.stipend}\nCTC: ${currentJob.ctcLpa} LPA"
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            DetailChip(Icons.Default.LocationOn, currentJob.location)
                        }
                    }


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            DetailChip(
                                Icons.Default.Info,
                                "Min CGPA required: ${currentJob.minCgpa}"
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            DetailChip(
                                Icons.Default.Person,
                                if (currentJob.eligibilityType == "ALL")
                                    "All Students can apply"
                                else
                                    "For Unplaced students only"
                            )
                        }
                    }
                }
            }

            // Description Section

            if (!currentJob.description.isNullOrBlank()) {

                var descExpanded by remember { mutableStateOf(false) }

                Text(
                    "Job Description",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { descExpanded = !descExpanded }
                            .padding(16.dp)
                    ) {

                        Text(
                            if (descExpanded) "Hide Description ▲" else "Show Description ▼",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5E3BBF)
                        )

                        AnimatedVisibility(descExpanded) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentJob.description
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


            }


            // Instructions Section

            if (!currentJob.instructions.isNullOrBlank()) {

                var expanded by remember { mutableStateOf(false) }

                Text(
                    "Instructions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )



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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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

// Apply section
            if (!isAdmin) {

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Button(
                        onClick = {
                            navController.navigate("jobForm/$jobId")
                        },

                        enabled = applyState == ApplyState.CAN_APPLY,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            when (applyState) {
                                ApplyState.ALREADY_APPLIED -> "Applied"
                                ApplyState.BATCH_MISMATCH -> "Not for your batch"
                                ApplyState.BRANCH_MISMATCH -> "Not for your branch"
                                ApplyState.BACKLOG_RESTRICTED -> "Backlogs not allowed"
                                ApplyState.CGPA_LOW -> "CGPA not eligible"
                                ApplyState.PLACEMENT_RESTRICTED -> "Placement restriction"
                                ApplyState.JOB_CLOSED -> "Application closed"
                                ApplyState.CAN_APPLY -> "Apply"
                                ApplyState.GENDER_RESTRICTED -> "Only female candidates allowed"
                                ApplyState.DREAM_PACKAGE_RESTRICTED -> "Dream package restriction"

                            }
                        )
                    }
                }
            }


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

@Composable
fun FileItemCard(fileName: String, fileUrl: String) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
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
            Text(fileName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
