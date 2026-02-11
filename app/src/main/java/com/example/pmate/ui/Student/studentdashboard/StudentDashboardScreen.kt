package com.example.pmate.ui.Student.studentdashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.Auth.SessionManager
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun StudentDashboardScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    val context = LocalContext.current
    val session = LocalSessionManager.current

    val repo = remember { FirestoreRepository(session) }

    val scope = rememberCoroutineScope()

    var batch by remember { mutableStateOf("") }
    var totalStudents by remember { mutableStateOf(0) }
    var placedCount by remember { mutableStateOf(0) }
    var notPlacedCount by remember { mutableStateOf(0) }

    var totalCompanies by remember { mutableStateOf(0) }
    var activeCompanies by remember { mutableStateOf(0) }
    var holdCompanies by remember { mutableStateOf(0) }
    var completedCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {

            val student = repo.getCurrentStudent()
            batch = student.batchYear

            val students = repo.getStudentsByBatch(batch)
            totalStudents = students.size
            placedCount = students.count { it.placementStatus == "PLACED" }
            notPlacedCount = students.count { it.placementStatus != "PLACED" }

            val jobs = repo.getAllJobs()
                .filter { it.batchYear == batch }

            totalCompanies = jobs.groupBy { it.company }.size
            activeCompanies = jobs.count { it.status == "Active" }
            holdCompanies = jobs.count { it.status == "On Hold" }
            completedCount = jobs.count { it.status == "Completed" }

        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text("Student Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        Text("Batch: $batch", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(20.dp))

        // ---------------- STUDENT INSIGHTS ----------------

        Text("Student Insights", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        DashboardCard(
            title = "Total Students",
            value = totalStudents.toString()
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            DashboardCard(
                title = "Placed Students",
                value = placedCount.toString(),
                modifier = Modifier.weight(1f),
                showArrow = true
            ) {
                navController.navigate("PlacedStudents/$batch")
            }

            DashboardCard(
                title = "Not Placed",
                value = notPlacedCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(25.dp))

        // ---------------- COMPANY INSIGHTS ----------------

        Text("Company Insights", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        DashboardCard(
            title = "Total Companies",
            value = totalCompanies.toString()
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            DashboardCard(
                title = "Active Companies",
                value = activeCompanies.toString(),
                modifier = Modifier.weight(1f),
                showArrow = true
            ) {
                navController.navigate("ActiveCompanies/$batch")
            }

            DashboardCard(
                title = "On Hold",
                value = holdCompanies.toString(),
                modifier = Modifier.weight(1f),
                showArrow = true
            ) {
                navController.navigate("HoldCompanies/$batch")

            }
        }

        Spacer(Modifier.height(12.dp))

        DashboardCard(
            title = "Completed Companies",
            value = completedCount.toString(),
            showArrow = true
        ) {
            navController.navigate("CompletedCompanies/$batch")
        }

        Spacer(Modifier.height(25.dp))

        // ---------------- NOTICE BOARD ----------------

        Text("Notice Board", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        DashboardCard(
            title = "View Notices",
            value = "",
            showArrow = true
        ) {
            navController.navigate("StudentNotices")

        }
    }
}


@Composable
fun DashboardCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    showArrow: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .height(105.dp)
            .let {
                if (onClick != null) it.clickable { onClick() } else it
            }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (value.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (showArrow) {
                Text(
                    text = ">",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
