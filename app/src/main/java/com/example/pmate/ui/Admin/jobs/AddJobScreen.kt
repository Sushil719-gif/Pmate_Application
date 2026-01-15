package com.example.pmate.ui.Admin.jobs

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
fun AddJobScreen(
    navController: NavController,
    repository: FirestoreRepository = FirestoreRepository()
) {

    // STATES
    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var stipend by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val jobTypes = listOf("Full-Time", "Internship-based FT", "Internship-Plus FT")
    var selectedJobType by remember { mutableStateOf(jobTypes.first()) }

    var instructions by remember { mutableStateOf("") }


    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // File picker state (we keep it, but uploading is disabled)
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Pick multiple files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                selectedFiles = uris
            }
        }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Job") }) }
    ) { padding ->

        VerticalScrollableScreen(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Create New Job", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            InputField("Company Name", company) { company = it }
            InputField("Job Role", role) { role = it }
            InputField("Stipend / Package", stipend) { stipend = it }
            InputField("Location", location) { location = it }

            // Job Type Dropdown
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

            // Deadline Picker
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
                            pickDate(context) { date -> deadline = date }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Description Box
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Job Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                maxLines = 8
            )

            // Upload files button (UI only, storage disabled)
            OutlinedButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("*/*"))  // File picking works
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Upload Job Details (Disabled)")
            }

            // Instructions
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                maxLines = 8
            )


            // Submit
            Button(
                onClick = {

                    if (company.isBlank() || role.isBlank() || stipend.isBlank() ||
                        location.isBlank() || deadline.isBlank() || description.isBlank()
                    ) return@Button

                    loading = true

                    scope.launch {

                        // ---------------------------------------
                        // ❌ FILE UPLOADING DISABLED TEMPORARILY
                        // ---------------------------------------
                        // Firebase Storage requires billing plan.
                        // So for now we skip file uploads and save empty list.

                        val uploadedUrls = emptyList<String>()   // <-- IMPORTANT

                        // Create job object
                        val job = JobModel(
                            company = company,
                            role = role,
                            stipend = stipend,
                            location = location,
                            jobType = selectedJobType,
                            deadline = deadline,
                            description = description,
                            instructions = instructions,

                            files = uploadedUrls
                        )

                        // Save Job to Firestore
                        val jobId = repository.addJob(job)

                        loading = false

                        if (jobId != null) {
                            navController.popBackStack()
                            navController.navigate("adminJobs")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Create Job", fontSize = 18.sp)
                }
            }

        }
    }

}

@Composable
fun InputField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

fun pickDate(context: Context, onSelected: (String) -> Unit) {
    val c = Calendar.getInstance()
    val year = c.get(Calendar.YEAR)
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(context, { _, y, m, d ->
        onSelected("$d/${m + 1}/$y")
    }, year, month, day).show()
}
