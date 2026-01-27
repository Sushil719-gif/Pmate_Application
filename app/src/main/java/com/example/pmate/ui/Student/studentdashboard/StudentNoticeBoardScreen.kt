package com.example.pmate.ui.Student.studentdashboard



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
import com.example.pmate.Firestore.DataModels.NoticeModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository

import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentNoticeBoardScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val repo = remember { FirestoreRepository() }
    var notices by remember { mutableStateOf<List<NoticeModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val student = repo.getCurrentStudent()
        val fetchedNotices = repo.getAllNotices()
            .filter { it.batch == student.batchYear }

        notices = fetchedNotices.sortedByDescending { it.timestamp }
        loading = false
    }


    Scaffold { padding ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Notices",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    Text("No notices available")
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(notices) { notice ->
                            StudentNoticeCard(notice)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun StudentNoticeCard(notice: NoticeModel) {

    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateString = sdf.format(Date(notice.timestamp))

    // Convert multiline message → bullet list
    val bulletPoints = notice.message
        .split("\n")
        .filter { it.isNotBlank() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            // TITLE
            Text(
                text = notice.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // BULLET POINT ITEMS
            bulletPoints.forEach { point ->
                Text("• $point", fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(10.dp))

            // DATE
            Text(
                dateString,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
