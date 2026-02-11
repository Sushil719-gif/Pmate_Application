package com.example.pmate.ui.Admin.jobs

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobActionScreen(
    navController: NavController,
    jobId: String
) {
    val context = LocalContext.current
    val session = LocalSessionManager.current

    val repo = remember { FirestoreRepository(session) }

    val scope = rememberCoroutineScope()


    var showArchiveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Job Actions") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Choose what you want to do with this job",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(30.dp))

            // ARCHIVE BUTTON (safe action)
            Button(
                onClick = { showArchiveDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4) // calm purple
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Archive Job", color = Color.White)
            }
            Spacer(Modifier.height(30.dp))
            // DELETE BUTTON (dangerous)
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Delete Permanently", color = Color.White)
            }
        }
    }

    // -------- Archive Confirmation --------
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Archive Job?") },
            text = {
                Text(
                    "This job will be archived and removed from operational lists.\n\n" +
                            "History will be preserved and it can be restored later."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchiveDialog = false
                        scope.launch {
                            repo.archiveJob(jobId)
                            Toast.makeText(
                                context,
                                "Job archived",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                            navController.popBackStack()
                        }
                    }
                ) { Text("Archive") }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // -------- Permanent Delete Confirmation --------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Permanent Delete?") },
            text = {
                Text(
                    "This will permanently delete this job.\n\n" +
                            "This action cannot be undone and all history will be lost."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            repo.deleteJobPermanently(jobId)
                            Toast.makeText(
                                context,
                                "Job permanently deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                            navController.popBackStack()
                        }
                    }
                ) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
