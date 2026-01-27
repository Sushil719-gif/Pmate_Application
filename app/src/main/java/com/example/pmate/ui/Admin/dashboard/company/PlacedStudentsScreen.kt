package com.example.pmate.ui.Admin.dashboard.company



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pmate.Firestore.DataModels.StudentModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction



@Composable
fun PlacedStudentsScreen(
    navController: NavController,
    batch: String
) {

    val repo = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()

    var data by remember {
        mutableStateOf<List<Pair<StudentModel, List<String>>>>(emptyList())
    }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(batch) {
        scope.launch {
            data = repo.getPlacedStudentsWithCompanies(batch)
        }
    }

    val filtered = data.filter { (student, companies) ->
        student.name.contains(searchQuery, true) ||
                student.usn.contains(searchQuery, true) ||
                companies.any { it.contains(searchQuery, true) }
    }

    Column(Modifier.padding(16.dp)) {

        Text(
            "Placed Students - $batch",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by Name / USN / Company") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { })
        )

        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No students found for \"$searchQuery\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered) { (student, companies) ->
                    StudentCompanyCard(student, companies)
                }
            }
        }
    }
}


@Composable
fun StudentCompanyCard(
    student: StudentModel,
    companies: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(student.name, fontWeight = FontWeight.Bold)
            Text("USN: ${student.usn}")
            Text("Companies: ${companies.joinToString()}")
        }
    }
}
