package com.example.pmate.ui.Admin.jobs



import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import com.example.pmate.ui.Student.studentjobs.FileItemCard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pmate.CommonReusableUIComponents.JobHeader
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.Navigation.Screen


import com.example.pmate.Scrolls.VerticalScrollableScreen
import com.example.pmate.ui.Student.studentjobs.DetailChip
import com.example.pmate.ui.Student.studentjobs.DetailSection

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminJobDetailsScreen(
    navController: NavController,
    jobId: String
) {

    val repo = remember { FirestoreRepository() }
    var job by remember { mutableStateOf<JobModel?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            job = repo.getJobById(jobId)
            loading = false
        }
    }

    Scaffold(
        topBar = {
            val navBackStackEntry = navController.currentBackStackEntryAsState().value
            val showBackArrow = shouldShowBackArrow(navBackStackEntry?.destination?.route)

            TopAppBar(
                title = { Text("Job Details") },   // 🔹 Change title as per screen
                navigationIcon = {
                    if (showBackArrow) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }

    ) { padding ->

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val j = job ?: return@Scaffold

        VerticalScrollableScreen(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            JobHeader(
                company = j.company,
                role = j.role,
                batch = j.batchYear,
                j.deadline
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    DetailChip(Icons.Default.Money, j.stipend)
                }
                Box(Modifier.weight(1f)) {
                    DetailChip(Icons.Default.LocationOn, j.location)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    DetailChip(Icons.Default.CalendarToday, "${j.batchYear} Batch")
                }
                Box(Modifier.weight(1f)) {
                    DetailChip(
                        Icons.Default.Person,
                        if (j.eligibilityType == "ALL")
                            "Eligibility: All Students"
                        else
                            "Eligibility: Unplaced Only"
                    )
                }
            }





            Divider()

            // Job Description Section
            if (!j.description.isNullOrBlank()) {

                var descExpanded by remember { mutableStateOf(false) }

                Text(
                    "Job Description",
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
                            .fillMaxWidth()
                            .clickable { descExpanded = !descExpanded }
                            .padding(16.dp)
                    ) {

                        Text(
                            text = if (descExpanded) "Hide Description ▲" else "Show Description ▼",
                            color = Color(0xFF5E3BBF),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )

                        AnimatedVisibility(visible = descExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                j.description
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

                Divider()
            }




//  Instructions Section
            if (!j.instructions.isNullOrBlank()) {

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
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(16.dp)
                    ) {

                        Text(
                            text = if (expanded) "Hide Instructions ▲" else "Show Instructions ▼",
                            color = Color(0xFF5E3BBF),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )

                        AnimatedVisibility(visible = expanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                // Convert multiline text into bullet points
                                j.instructions
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

                Divider()
            }




            //  Job Files Section
            if (j.files.isNotEmpty()) {
                Text("Job Files", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    j.files.forEachIndexed { index, url ->
                        FileItemCard(fileName = "File ${index + 1}", fileUrl = url)
                    }
                }

                Spacer(Modifier.height(20.dp))
            }


            Spacer(Modifier.height(20.dp))



            // EDIT BUTTON
            Button(
                onClick = { navController.navigate("editJob/${j.jobId}") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Edit Job")
            }

            // VIEW APPLICANTS BUTTON
            Button(
                onClick = { navController.navigate("applicants/${j.jobId}") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.List, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("View Applicants")
            }

            // DELETE BUTTON
            Button(
                onClick = { navController.navigate("job_action/${j.jobId}") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete / Archive", color = Color.White)
            }


        }
    }
}
fun shouldShowBackArrow(route: String?): Boolean {
    // Screens where BACK ARROW should NOT appear
    val noBackScreens = listOf(
        Screen.RoleSelection.route,
        Screen.AdminMain.route,
        Screen.StudentMain.route
    )
    return route !in noBackScreens
}