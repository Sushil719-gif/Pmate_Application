package com.example.pmate.ui.Admin.jobs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
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
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.Auth.SessionManager
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

    val context = LocalContext.current
    val session = LocalSessionManager.current

    val repo = remember { FirestoreRepository(session) }

    var job by remember { mutableStateOf<JobModel?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()


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

    // Branch selection
    val branchOptions = listOf("CSE", "IT", "ECE", "EEE", "MECH", "CIVIL")
    var selectedBranches by remember { mutableStateOf(j.branches.toSet()) }
    var branchExpanded by remember { mutableStateOf(false) }


    // Prefilled states
    var company by remember { mutableStateOf(j.company) }
    var role by remember { mutableStateOf(j.role) }
    var stipend by remember { mutableStateOf(j.stipend) }
    var location by remember { mutableStateOf(j.location) }
    var batchYear by remember { mutableStateOf(j.batchYear) }

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
            BatchYearDropdown(
                selectedYear = batchYear,
                onYearSelected = { batchYear = it }
            )


//            Text(
//                text = "Eligible Branches",
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 16.sp
//            )

            ExposedDropdownMenuBox(
                expanded = branchExpanded,
                onExpandedChange = { branchExpanded = !branchExpanded }
            ) {
                OutlinedTextField(
                    value = if (selectedBranches.isEmpty())
                        "Select Branches"
                    else
                        selectedBranches.joinToString(", "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Branches") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(branchExpanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = branchExpanded,
                    onDismissRequest = { branchExpanded = false }
                ) {
                    branchOptions.forEach { branch ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedBranches.contains(branch),
                                        onCheckedChange = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(branch)
                                }
                            },
                            onClick = {
                                selectedBranches =
                                    if (selectedBranches.contains(branch))
                                        selectedBranches - branch
                                    else
                                        selectedBranches + branch
                            }
                        )
                    }
                }
            }





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
                            pickEditDateTime(context) { picked ->
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
                        batchYear = batchYear,
                        branches = selectedBranches.toList(),
                        deadline = deadline,
                        jobType = selectedJobType,
                        description = description,
                        instructions = instructions
                    )

                    scope.launch {
                        repo.updateJob(updatedJob)
                        updating = false
                        Toast.makeText(
                            context,
                            "Job edited successfully ",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
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

//  UNIQUE Edit Input Field
@Composable
fun EditInputField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

//  UNIQUE Edit Date Picker
fun pickEditDateTime(
    context: Context,
    onSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->

            TimePickerDialog(
                context,
                { _, hour, minute ->

                    val selectedCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }

                    val format = java.text.SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    )

                    onSelected(format.format(selectedCal.time))

                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()

        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

