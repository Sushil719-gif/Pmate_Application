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
import java.util.Calendar

import com.example.pmate.ThreeBatchesAccess.BatchUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment


@Composable
fun AdminDashboardScreen(
    navController: NavController,
    selectedBatch: String,
    onBatchChange: (String) -> Unit
)



 {


    val batchOptions = BatchUtils.getAllowedBatches()



     var batchMenuExpanded by remember { mutableStateOf(false) }


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

        Spacer(Modifier.height(12.dp))

        // -------- Batch Selector --------
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            // Batch
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { batchMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batch: $selectedBatch")
                }

                DropdownMenu(
                    expanded = batchMenuExpanded,
                    onDismissRequest = { batchMenuExpanded = false }
                ) {
                    batchOptions.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year) },
                            onClick = {
                                onBatchChange(year)
                                batchMenuExpanded = false
                            }
                        )
                    }
                }
            }


        }


        Spacer(Modifier.height(20.dp))

        // ---------- STUDENT SECTION ----------
        Text("Student Insights", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        StudentSection(navController, selectedBatch)


        Spacer(Modifier.height(25.dp))

        // ---------- COMPANY SECTION ----------
        Text("Company Insights", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        CompanySection(navController, selectedBatch)


        Spacer(Modifier.height(25.dp))

        // ---------- NOTICE BOARD SECTION ----------
        Text("Notice Board", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        NoticeBoardSection(navController, selectedBatch)

    }
}



// ------------------------ STUDENT SECTION ------------------------

@Composable
fun StudentSection(
    navController: NavController,
    batch: String
)

 {

    val repo = remember { FirestoreRepository() }
    var total by remember { mutableStateOf(0) }
    var placed by remember { mutableStateOf(0) }
    var notPlaced by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

     LaunchedEffect(batch) {
         scope.launch {
             val students = repo.getStudentsByBatch(batch)


             total = students.size
             placed = students.count { it.placementStatus == "PLACED" }
             notPlaced = students.count { it.placementStatus != "PLACED" }
         }
     }


     Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        DashboardCard(
            title = "Total Students",
            value = total.toString(),

        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardCard(
                title = "Placed Students",
                value = placed.toString(),
                modifier = Modifier.weight(1f)
            ) {
                navController.navigate("PlacedStudents/$batch")

            }

            DashboardCard(
                title = "Not Placed",
                value = notPlaced.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}



// ------------------------ COMPANY SECTION ------------------------

@Composable
fun CompanySection(
    navController: NavController,
    batch: String
)
 {

    val repo = remember { FirestoreRepository() }
    var totalCompanies by remember { mutableStateOf(0) }
    var activeCount by remember { mutableStateOf(0) }
    var holdCount by remember { mutableStateOf(0) }
    var completedCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

     LaunchedEffect(batch) {
         scope.launch {
             val jobs = repo.getAllJobs()
                 .filter {
                     it.batchYear == batch
                 }

             val grouped = jobs.groupBy { it.company }

             totalCompanies = grouped.size
             activeCount = jobs.count { it.status == "Active" }
             holdCount = jobs.count { it.status == "On Hold" }
             completedCount = jobs.count { it.status == "Completed" }
         }
     }


    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        DashboardCard(
            title = "Total Companies visited so far",
            value = totalCompanies.toString()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            DashboardCard(
                title = "Active",
                value = activeCount.toString(),
                modifier = Modifier.weight(1f)
            ){
                navController.navigate("ActiveCompanies/$batch")
            }

            DashboardCard(
                title = "On Hold",
                value = holdCount.toString(),
                modifier = Modifier.weight(1f)
            ){
                navController.navigate("HoldCompanies/$batch")
            }
        }

        DashboardCard(
            title = "Completed Companies",
            value = completedCount.toString()
        ){
            navController.navigate("CompletedCompanies/$batch")
        }
    }
}



// ------------------------ NOTICE BOARD SECTION ------------------------

@Composable
fun NoticeBoardSection(
    navController: NavController,
    batch: String
)
 {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        DashboardCard(
            title = "Send Notice",
            value = "",
        ) {
            navController.navigate("SendNotice/$batch")

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
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .height(105.dp)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                if (value.isNotEmpty()) {
                    Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ➜ Arrow only if clickable
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}


