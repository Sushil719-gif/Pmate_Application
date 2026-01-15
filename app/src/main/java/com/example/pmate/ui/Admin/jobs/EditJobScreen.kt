package com.example.pmate.ui.Admin.jobs

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
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
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.Scrolls.VerticalScrollableScreen
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJobScreen(
    navController: NavController,
    jobId: String
) {

    val repo = remember { FirestoreRepository() }
    var job by remember { mutableStateOf<JobModel?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Fetch job first
    LaunchedEffect(Unit) {
        scope.launch {
            job = repo.getJobById(jobId)
            loading = false
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val j = job ?: return

    // Prefilled states
    var company by remember { mutableStateOf(j.company) }
    var role by remember { mutableStateOf(j.role) }
    var stipend by remember { mutableStateOf(j.stipend) }
    var location by remember { mutableStateOf(j.location) }
    var deadline by remember { mutableStateOf(j.deadline) }
    var description by remember { mutableStateOf(j.description) }
    var instructions by remember { mutableStateOf(j.instructions ?: "") }

    val jobTypes = listOf("Full-Time", "Internship-based FT", "Internship-Plus FT")
    var selectedJobType by remember { mutableStateOf(j.jobType) }
    var expanded by remember { mutableStateOf(false) }

    var updating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Job") }) }
    ) { padding ->

        VerticalScrollableScreen(
            modifier = Modifier
                .padding(padding)
                .background(Color(0xFFF7F7F7))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Edit Job Details", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            // Use EDIT input field to avoid conflicts
            EditInputField("Company Name", company) { company = it }
            EditInputField("Job Role", role) { role = it }
            EditInputField("Stipend / Package", stipend) { stipend = it }
            EditInputField("Location", location) { location = it }

            // Job type dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {

                OutlinedTextField(
                    value = selectedJobType,
                    onValueChange = {},
                    label = { Text("Job Type") },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    jobTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedJobType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Deadline
            OutlinedTextField(
                value = deadline,
                onValueChange = {},
                label = { Text("Application Deadline") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            pickEditDate(context) { picked ->
                                deadline = picked
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Job Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 8
            )

            // Instructions
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 8
            )

            // Save button
            Button(
                onClick = {
                    updating = true

                    val updatedJob = j.copy(
                        company = company,
                        role = role,
                        stipend = stipend,
                        location = location,
                        deadline = deadline,
                        jobType = selectedJobType,
                        description = description,
                        instructions = instructions
                    )

                    scope.launch {
                        repo.updateJob(updatedJob)
                        updating = false

                        navController.navigate("job_details_admin/$jobId") {
                            popUpTo("adminJobs") { inclusive = false }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (updating) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Changes", fontSize = 18.sp)
                }
            }
        }
    }
}

// 🚀 UNIQUE Edit Input Field
@Composable
fun EditInputField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

// 🚀 UNIQUE Edit Date Picker
fun pickEditDate(context: Context, onSelected: (String) -> Unit) {
    val c = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, y, m, d -> onSelected("$d/${m + 1}/$y") },
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH),
        c.get(Calendar.DAY_OF_MONTH)
    ).show()
}
