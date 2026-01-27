package com.example.pmate.ui.Admin.dashboard.company

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.Typeface
import androidx.navigation.NavController
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun CompletedCompaniesListScreen(
    navController: NavController,
    batch: String
) {
    val repo = remember { FirestoreRepository() }
    var companies by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(batch) {
        scope.launch {
            val jobs = repo.getAllJobs()
                .filter { it.batchYear == batch && it.status == "Completed" }


            companies = jobs.map { it.company }.distinct()
        }
    }


    CompanyAnalyticsList(
        title = "Completed Companies",
        companies = companies,
        batch = batch,
        type = "COMPLETED"
    )
}
