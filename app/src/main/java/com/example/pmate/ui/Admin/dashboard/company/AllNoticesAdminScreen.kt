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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.Firestore.DataModels.NoticeModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.sortedByDescending

@Composable
fun AllNoticesAdminScreen(navController: NavController) {

    val repo = remember { FirestoreRepository() }
    var notices by remember { mutableStateOf<List<NoticeModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            notices = repo.getAllNotices().sortedByDescending { it.timestamp }
            loading = false
        }
    }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding)
        ) {

            Text("All Notices", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))

            when {
                loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                notices.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notices yet")
                }

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notices) { notice ->
                        NoticeCard(notice)
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeCard(notice: NoticeModel) {

    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateString = sdf.format(Date(notice.timestamp))

    val bulletPoints = notice.message.split("\n").filter { it.isNotBlank() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(notice.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Text(
                "Batch: ${notice.batch}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(6.dp))

            bulletPoints.forEach { point ->
                Text("• $point", fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(8.dp))

            Text(
                dateString,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
