package com.example.pmate.ui.Admin.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.Auth.SessionManager
import com.example.pmate.Firestore.DataModels.CsvUploadResult
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.ui.Admin.jobs.BatchYearDropdown
import kotlinx.coroutines.launch




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadResultScreen(navController: NavController) {


    val context = LocalContext.current
    val session = LocalSessionManager.current

    val repo = remember { FirestoreRepository(session) }

    val scope = rememberCoroutineScope()

    var selectedBatch by rememberSaveable { mutableStateOf<String?>(null) }

    var fileUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val fileUri = fileUriString?.let { Uri.parse(it) }

    var uploadResult by remember { mutableStateOf<CsvUploadResult?>(null) }



    val snackbarHostState = remember { SnackbarHostState() }
    var loading by remember { mutableStateOf(false) }

    // When screen opens OR batch changes → load last upload history from Firestore
    LaunchedEffect(selectedBatch) {
        selectedBatch?.let { batch ->
            try {
                uploadResult = repo.getLastCsvUploadForBatch(batch)
            } catch (e: Exception) {
                uploadResult = null
            }
        }
    }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        fileUriString = uri?.toString()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Import Student Academic Data") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ---------- INFO CARD ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Bulk Academic Update",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E3BBF)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Upload the latest result sheet to update CGPA, backlogs and names for a selected batch.\n\nYou can undo changes for 24 hours after upload.",
                        fontSize = 14.sp
                    )
                }
            }

            Text("Steps:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text("1. Select the batch year")
            Text("2. Choose the CSV result file")
            Text("3. Upload and verify the update")

            // ---------- BATCH SELECTOR ----------
            BatchYearDropdown(
                selectedYear = selectedBatch ?: "Select Batch",
                onYearSelected = { selectedBatch = it }
            )

            // ---------- FILE PICKER ----------
            Button(
                onClick = { launcher.launch(arrayOf("text/*")) },
                enabled = selectedBatch != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select CSV File")
            }

            // ---------- UPLOAD ----------
            fileUri?.let {

                Text(
                    "Selected File: ${it.lastPathSegment}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Button(
                    onClick = {
                        loading = true
                        scope.launch {

                            uploadResult =
                                repo.processResultCsv(context, fileUri!!, selectedBatch!!)

                            loading = false

                            snackbarHostState.showSnackbar(
                                "$selectedBatch batch students updated successfully"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (loading)
                        CircularProgressIndicator(color = Color.White)
                    else
                        Text("Upload & Update Students")
                }
            }

            // ---------- RESULT COUNTER CARD ----------
            uploadResult?.let { result ->

                val isUndoExpired =
                    System.currentTimeMillis() > result.expiresAt

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            "Upload Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Text("Updated Students : ${result.updated}")
                        Text("Skipped (Wrong Batch) : ${result.skipped}")
                        Text("Not Found : ${result.notFound}")

                        Spacer(Modifier.height(10.dp))

                        Text(
                            if (isUndoExpired)
                                "Changes are now permanent and cannot be reverted."
                            else
                                "You can undo these changes for the next 24 hours.",
                            fontWeight = FontWeight.Medium,
                            color = if (isUndoExpired) Color.Red else Color(0xFF2E7D32)
                        )
                    }
                }

                // ---------- UNDO BUTTON ----------
                Button(
                    onClick = {
                        scope.launch {
                            repo.undoCsvUpload(result.uploadId)
                            uploadResult = null

                            snackbarHostState.showSnackbar(
                                "Changes reverted successfully"
                            )
                        }
                    },
                    enabled = !isUndoExpired,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isUndoExpired)
                            "Undo Expired"
                        else
                            "Undo Changes"
                    )
                }
            }
        }
    }
}
