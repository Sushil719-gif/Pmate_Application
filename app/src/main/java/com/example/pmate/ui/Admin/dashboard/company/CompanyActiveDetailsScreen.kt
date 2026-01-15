package com.example.pmate.ui.Admin.dashboard.company



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun CompanyActiveDetailsScreen(
    navController: NavController,
    companyName: String
) {
    val repo = remember { FirestoreRepository() }
    var job by remember { mutableStateOf<JobModel?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            job = repo.getJobByCompany(companyName)   // returns one job or null
            loading = false
        }
    }

    Scaffold { padding ->

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            job == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text("No Active Job Details Found") }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(padding)
                ) {

                    Text(
                        text = job!!.company,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailItem("Role", job!!.role)
                    DetailItem("Package / Stipend", job!!.stipend)
                    DetailItem("Location", job!!.location)
                    DetailItem("Deadline", job!!.deadline)
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = value, fontSize = 14.sp)
}
