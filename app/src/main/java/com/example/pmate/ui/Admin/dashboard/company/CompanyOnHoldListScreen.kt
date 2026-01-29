package com.example.pmate.ui.Admin.dashboard.company





import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.navigation.NavController
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun CompanyOnHoldListScreen(
    navController: NavController,
    batch: String
) {
    val repo = remember { FirestoreRepository() }
    var companies by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(batch) {
        scope.launch {
            val jobs = repo.getAllJobs()
                .filter {
                    it.batchYear == batch &&
                            it.status == "On Hold"
                }

            companies = jobs.map { it.company }.distinct()
        }
    }

    CompanyAnalyticsList(
        title = "Companies On Hold",
        companies = companies,
        batch = batch,
        type = "HOLD"
    )
}
