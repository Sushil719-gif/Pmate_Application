package com.example.pmate.ui.Admin.dashboard.company

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pmate.Firestore.DataModels.CompanyAnalytics
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun CompanyAnalyticsList(
    title: String,
    companies: List<String>,
    batch: String,
    type: String   // "ACTIVE" | "HOLD" | "COMPLETED"
)
 {
    val repo = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()

    var analyticsList by remember {
        mutableStateOf<List<CompanyAnalytics>>(emptyList())
    }

    var loading by remember { mutableStateOf(true) }

    // ✅ Fetch everything once
    LaunchedEffect(companies) {
        scope.launch {
            val list = mutableListOf<CompanyAnalytics>()
            companies.forEach { company ->
                val data = repo.getCompanyAnalytics(company, batch)
                list.add(data)
            }
            analyticsList = list
            loading = false
        }
    }

    Column(Modifier.padding(16.dp)) {

        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            analyticsList.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No companies found")
                }
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(analyticsList) { data ->
                        CompanyAnalyticsCard(data,type)
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyAnalyticsCard(
    data: CompanyAnalytics,
    type: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(data.company, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(6.dp))

            Text("Role: ${data.role}")
            Text("Location: ${data.location}")

            Spacer(Modifier.height(8.dp))

            Text("Applicants: ${data.applicants}")

            // ✅ Only for completed
            if (type == "COMPLETED") {
                Text("Placed: ${data.placed}")
                Text("Selection Rate: ${data.selectionRate}%")
            }
        }
    }
}

