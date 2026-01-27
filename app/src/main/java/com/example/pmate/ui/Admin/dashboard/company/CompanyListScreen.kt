package com.example.pmate.ui.Admin.dashboard.company



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

data class Company(
    val name: String,
    val status: String = "Active" // Active | On Hold | Completed
)

@Composable
fun CompanyListScreen(navController: NavController,batch: String) {

    val snackbarHostState = remember { SnackbarHostState() }
    val repo = remember { FirestoreRepository() }
    var companyList by remember { mutableStateOf<List<Company>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val jobs = repo.getJobsByBatch(batch)


            // Group by company name
            val grouped = jobs.groupBy { it.company }

            // Convert to Company objects
            companyList = grouped.map { (companyName, jobGroup) ->
                Company(
                    name = companyName,
                    status = jobGroup.first().status  // take status from first job entry
                )
            }

            loading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding)
        ) {

            Text(
                text = "Total Companies",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                companyList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Companies Found")
                    }
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(companyList) { company ->
                            CompanyItem(
                                company = company,
                                navController = navController,
                                snackbarHostState = snackbarHostState,
                                batch = batch
                            )

                        }
                    }
                }
            }
        }
    }
}



@Composable
fun CompanyItem(
    company: Company,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    batch: String
)
 {
val scope =rememberCoroutineScope()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (company.status) {
                    "Active" -> navController.navigate("CompanyActiveDetails/${company.name}/$batch")
                    "Completed" -> navController.navigate("CompanyDetails/${company.name}/$batch")
                    "On Hold" -> {
                        scope.launch {
                            snackbarHostState.showSnackbar("Company is On Hold")
                        }
                    }
                }

            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(company.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Status: ${company.status}", fontSize = 14.sp)
            }
        }
    }
}


