package com.example.pmate.ui.Student.studentdashboard




import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun StudentDashboardScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Student Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ---------- STUDENT INSIGHTS ----------
        Text(
            text = "Student Insights",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))
        StudentInsightsSection(navController)

        Spacer(modifier = Modifier.height(25.dp))

        // ---------- COMPANY INSIGHTS ----------
        Text(
            text = "Company Insights",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))
        CompanyInsightsSection(navController)

        Spacer(modifier = Modifier.height(25.dp))

        // ---------- NOTICE BOARD ----------
        Text(
            text = "Notice Board",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))
        StudentNoticeBoardSection(navController)
    }
}

@Composable
fun StudentInsightsSection(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        DashboardCard(title = "Total Students (All Batches)", value = "0") {
            navController.navigate("StudentBatchList")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardCard(
                title = "Placed Students",
                value = "0",
                modifier = Modifier.weight(1f)
            ) {
                navController.navigate("PlacedStudents")
            }

            DashboardCard(
                title = "Not Placed",
                value = "0",
                modifier = Modifier.weight(1f)
            ) {
                navController.navigate("NonPlacedStudents")
            }
        }
    }
}

@Composable
fun CompanyInsightsSection(navController: NavController) {

    val repo = remember { FirestoreRepository() }
    var companyCount by remember { mutableStateOf(0) }
    var holdCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val jobs = repo.getAllJobs()
            val grouped = jobs.groupBy { it.company }
            companyCount = grouped.size
            holdCount = jobs.count { it.status == "On Hold" }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        DashboardCard(
            title = "Total Companies",
            value = companyCount.toString()
        ) {
            navController.navigate("CompanyList")
        }

        DashboardCard(
            title = "Companies On Hold",
            value = holdCount.toString()
        ) {
            navController.navigate("CompanyHoldList")
        }


    }
}

@Composable
fun StudentNoticeBoardSection(navController: NavController) {

    val repo = remember { FirestoreRepository() }
    var noticeCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            noticeCount = repo.getAllNotices().size
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        DashboardCard(
            title = "Total Notices",
            value = noticeCount.toString()
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
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .height(105.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            if (value.isNotEmpty()) {
                Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
