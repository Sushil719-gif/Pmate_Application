package com.example.pmate.ui.Admin.dashboard.company

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.launch

@Composable
fun SendNoticeScreen(
    navController: NavController,
    batch: String
) {

    val repo = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Validity
    var validityMenuExpanded by remember { mutableStateOf(false) }
    var validDays by remember { mutableStateOf(3) }

    // Level
    var levelMenuExpanded by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf("INFO") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Send Notice - Batch $batch",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Notice Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            minLines = 5,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 🔹 Validity Dropdown
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { validityMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Valid: $validDays days")
                }

                DropdownMenu(
                    expanded = validityMenuExpanded,
                    onDismissRequest = { validityMenuExpanded = false }
                ) {
                    listOf(2, 3, 5).forEach { days ->
                        DropdownMenuItem(
                            text = { Text("$days days") },
                            onClick = {
                                validDays = days
                                validityMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // 🔹 Level Dropdown
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { levelMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Level: $level")
                }

                DropdownMenu(
                    expanded = levelMenuExpanded,
                    onDismissRequest = { levelMenuExpanded = false }
                ) {
                    listOf("INFO", "IMPORTANT", "CRITICAL").forEach { lvl ->
                        DropdownMenuItem(
                            text = { Text(lvl) },
                            onClick = {
                                level = lvl
                                levelMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }



        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (title.isNotEmpty() && message.isNotEmpty()) {
                    isLoading = true
                    scope.launch {
                        repo.addNotice(title, message, batch, validDays, level)
                        isLoading = false
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Send Notice")
            }
        }
    }
}
