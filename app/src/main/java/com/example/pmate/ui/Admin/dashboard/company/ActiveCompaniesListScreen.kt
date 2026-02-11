package com.example.pmate.ui.Admin.dashboard.company

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.Auth.SessionManager
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun ActiveCompaniesListScreen(
    navController: NavController,
    batch: String
) {
    val context = LocalContext.current
    val session = LocalSessionManager.current

    val repo = remember { FirestoreRepository(session) }

    var companies by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(batch) {
        scope.launch {
            val jobs = repo.getAllJobs()
                .filter {
                    it.batchYear == batch &&
                            it.status == "Active"
                }

            companies = jobs.map { it.company }.distinct()
        }
    }

    CompanyAnalyticsList(
        title = "Active Companies",
        companies = companies,
        batch = batch,
        type = "ACTIVE"
    )
}
