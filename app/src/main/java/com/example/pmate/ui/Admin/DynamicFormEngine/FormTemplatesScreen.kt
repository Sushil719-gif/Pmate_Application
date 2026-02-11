package com.example.pmate.ui.Admin.DynamicFormEngine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.CommonReusableUIComponents.PmateTopBar
import com.example.pmate.Firestore.DataModels.FormTemplate
import com.example.pmate.Firestore.FirestoreRepository.FormRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTemplatesScreen(
    navController: NavController
) {
    val session = LocalSessionManager.current
    val repo = remember { FormRepository(session.currentCollegeId) }

    var templates by remember { mutableStateOf<List<FormTemplate>>(emptyList()) }
    var title by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<FormTemplate?>(null) }

    LaunchedEffect(Unit) {
        repo.getTemplates {
            templates = it
        }
    }

    Scaffold(
        topBar = {
            PmateTopBar(
                title = "Form Templates",
                navController = navController
            )
        }
    ) { padding ->

        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Spacer(Modifier.height(10.dp))

                Text(
                    "You can create multiple templates and reuse them while adding jobs with a single selection.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("New Template Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        repo.createTemplate(title.trim()) { id ->
                            title = ""
                            navController.navigate("templateBuilder/$id")
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Create New Template")
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Your Templates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(templates) { template ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("templateBuilder/${template.templateId}")
                            }
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {
                            Text(
                                template.title,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Tap to view/manage fields",
                                color = Color.Gray
                            )

                        }

                        IconButton(
                            onClick = {
                                templateToDelete = template
                                showDeleteDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    //  Confirmation Dialog
    if (showDeleteDialog && templateToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        repo.deleteTemplate(
                            templateId = templateToDelete!!.templateId,
                            onDone = {
                                templates = templates.filter {
                                    it.templateId != templateToDelete!!.templateId
                                }
                                showDeleteDialog = false

                                android.widget.Toast
                                    .makeText(
                                        navController.context,
                                        "Template deleted successfully",
                                        android.widget.Toast.LENGTH_SHORT
                                    )
                                    .show()
                            },
                            onError = {
                                showDeleteDialog = false
                            }
                        )
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Template?") },
            text = {
                Text("This will permanently delete the template and all its fields.")
            }
        )
    }
}
