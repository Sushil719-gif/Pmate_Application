package com.example.pmate.ui.Admin.jobs

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.ui.window.Dialog

import com.example.pmate.Firestore.DataModels.Applicant
import com.example.pmate.Firestore.DataModels.OADetails
import com.example.pmate.Firestore.DataModels.StudentModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantsListScreen(
    navController: NavController,
    jobId: String
) {
    val context = LocalContext.current
    val session = LocalSessionManager.current
    var showOADialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val repo = remember { FirestoreRepository(session) }

    val scope = rememberCoroutineScope()
    var showPackageDialogFor by remember { mutableStateOf<Pair<String, String>?>(null) }
// Pair(applicationId, studentId)

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
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
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
            if (selectedFilter == "SHORTLISTED") {
                OutlinedButton(onClick = { showOADialog = true }) {
                    Text("Update Venue Details")
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
                            oaDetails = applicant.oaDetails,

                            //Entering package of selected student
                            onUpdateStatus = { newStatus ->
                                if (newStatus == "PLACED") {
                                    showPackageDialogFor = Pair(applicationId, applicant.studentId)
                                } else {
                                    scope.launch {
                                        repo.updateApplicationStatus(
                                            applicationId = applicationId,
                                            studentId = applicant.studentId,
                                            status = newStatus
                                        )
                                    }
                                }
                            }


                        )
                    }
                }
            }

        }
        showPackageDialogFor?.let { (applicationId, studentId) ->

            PackageInputDialog(
                onConfirm = { lpa ->
                    scope.launch {
                        repo.addOfferToStudent(studentId, lpa)

                        repo.updateApplicationStatus(
                            applicationId = applicationId,
                            studentId = studentId,
                            status = "PLACED"
                        )

                        showPackageDialogFor = null
                    }
                },
                onDismiss = {
                    showPackageDialogFor = null
                }
            )
        }
        if (showOADialog) {
            OAVenueDialog(
                applicants = shortlisted,   // only shortlisted
                onAssign = { roomMap, oa ->
                    scope.launch {
                        repo.assignOAToApplicants(roomMap, oa)
                        showOADialog = false

                        snackbarHostState.showSnackbar(
                            "Venue assigned to ${roomMap.size} students"
                        )
                    }
                },

                onDismiss = { showOADialog = false }
            )
        }


    }
}

@Composable
fun ApplicantCard(
    student: StudentModel,
    status: String,
    isNew: Boolean,
    oaDetails: OADetails?,
    onUpdateStatus: (String) -> Unit
)
 {
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
            if (oaDetails != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Venue: ${oaDetails.room}",
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Medium
                )
            }


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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OAVenueDialog(
    applicants: List<Pair<String, Pair<Applicant, StudentModel>>>,
    onAssign: (Map<String, String>, OADetails) -> Unit,
    onDismiss: () -> Unit
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf("") }


    var rooms by remember {
        mutableStateOf(listOf<Pair<String, Int>>())
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            var error by remember { mutableStateOf("") }

            TextButton(onClick = {

                val totalCapacity = rooms.sumOf { it.second }

                if (totalCapacity < applicants.size) {
                    error = "Total capacity is less than shortlisted students!"
                    return@TextButton
                }

                val applicationIds = applicants.map { it.first }
                val map = distributeRooms(applicationIds, rooms)

                val oa = OADetails(date, time, "", instructions)
                onAssign(map, oa)

            }) {

            Text("Auto Assign")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Assign OA Venue") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (error.isNotEmpty()) {
                    Text(error, color = Color.Red)
                }


                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, null)
                        }
                    }
                )
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    val sdf = java.text.SimpleDateFormat("dd-MM-yyyy")
                                    date = sdf.format(java.util.Date(millis))
                                }
                                showDatePicker = false
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }





                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Time") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.AccessTime, null)
                        }
                    }
                )

                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val hour = timePickerState.hour
                                val minute = timePickerState.minute
                                time = String.format("%02d:%02d", hour, minute)
                                showTimePicker = false
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Cancel")
                            }
                        },
                        text = {
                            TimePicker(state = timePickerState)
                        }
                    )
                }





                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions") }
                )


                Spacer(Modifier.height(8.dp))

                rooms.forEachIndexed { index, pair ->

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        OutlinedTextField(
                            value = pair.first,
                            onValueChange = { newRoom ->
                                rooms = rooms.toMutableList().also {
                                    it[index] = newRoom to pair.second
                                }
                            },
                            label = { Text("Room") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = if (pair.second == 0) "" else pair.second.toString(),
                            onValueChange = { newCap ->
                                val cap = newCap.toIntOrNull() ?: 0
                                rooms = rooms.toMutableList().also {
                                    it[index] = pair.first to cap
                                }
                            },
                            label = { Text("Capacity") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                TextButton(
                    onClick = { rooms = rooms + ("" to 0) }
                ) {
                    Text("+ Add Room")
                }

            }
        }

    )

}

fun distributeRooms(
    applicants: List<String>,
    rooms: List<Pair<String, Int>>
): Map<String, String> {

    val result = mutableMapOf<String, String>()
    var index = 0

    for ((room, cap) in rooms) {
        repeat(cap) {
            if (index >= applicants.size) return result
            result[applicants[index]] = room
            index++
        }
    }
    return result
}
