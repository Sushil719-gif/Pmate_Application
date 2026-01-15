package com.example.pmate.ui.Admin.dashboard

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
fun AdminDashboardScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 90.dp)

    ) {

        Text(
            text = "Admin Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ---------- STUDENT SECTION ----------
        Text(
            text = "Student Insights",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))
        StudentSection(navController)

        Spacer(modifier = Modifier.height(25.dp))

        // ---------- COMPANY SECTION ----------
        Text(
            text = "Company Insights",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))
        CompanySection(navController)

        Spacer(modifier = Modifier.height(25.dp))

        // ---------- NOTICE BOARD SECTION ----------
        Text(
            text = "Notice Board",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))
        NoticeBoardSection(navController)
    }
}


// ------------------------ STUDENT SECTION ------------------------

@Composable
fun StudentSection(navController: NavController) {
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


// ------------------------ COMPANY SECTION ------------------------

@Composable
fun CompanySection(navController: NavController) {

    val repo = remember { FirestoreRepository() }
    var companyCount by remember { mutableStateOf(0) }
    var holdCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val jobs = repo.getAllJobs()

            // Unique companies
            val grouped = jobs.groupBy { it.company }
            companyCount = grouped.size

            // Count On Hold
            holdCount = jobs.count { it.status == "On Hold" }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

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


// ------------------------ NOTICE BOARD SECTION ------------------------

@Composable
fun NoticeBoardSection(navController: NavController) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        DashboardCard(
            title = "Send Notice",
            value = "",
        ) {
            navController.navigate("SendNotice")
        }

        DashboardCard(
            title = "All Notices",
            value = "",
        ) {
            navController.navigate("AllNoticesAdmin")
        }
    }
}



// ------------------------ REUSABLE DASHBOARD CARD ------------------------

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
