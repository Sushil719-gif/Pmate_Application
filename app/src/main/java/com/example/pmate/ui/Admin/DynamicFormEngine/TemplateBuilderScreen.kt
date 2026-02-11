package com.example.pmate.ui.Admin.DynamicFormEngine

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.CommonReusableUIComponents.PmateTopBar
import com.example.pmate.Firestore.DataModels.FormField
import com.example.pmate.Firestore.FirestoreRepository.FormRepository
import kotlinx.coroutines.launch
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateBuilderScreen(
    templateId: String,
    navController: NavController
) {

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()


    val session = LocalSessionManager.current

    val repo = remember {
        FormRepository(session.currentCollegeId)
    }

    val viewModel: TemplateBuilderViewModel = viewModel(
        factory = TemplateBuilderVMFactory(repo)
    )


    var label by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("text") }
    var required by remember { mutableStateOf(false) }
    var optionsText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var templateTitle by remember { mutableStateOf("") }
    var editingTitle by remember { mutableStateOf(false) }


    val fieldTypes = listOf(
        "text",          // Name, college, anything
        "number",        // CGPA, marks, experience
        "email",         // email validation
        "phone",         // phone validation
        "textarea",      // SOP, why should we hire you
        "dropdown",      // single select
        "radio",         // single select (better UX than dropdown sometimes)
        "checkbox",      // multi select
        "date",          // DOB, availability
        "file",          // resume, marksheet
        "url"            // portfolio, github, linkedin
    )


    LaunchedEffect(Unit) {
        viewModel.loadTemplateFields(templateId)

        repo.getTemplateTitle(templateId) {
            templateTitle = it
        }

    }





    Scaffold(
        topBar = {
            PmateTopBar(
                title = "Design Template",
                navController = navController
            )
        }
    ) { padding ->

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------- Template Title ----------
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            "Template Title",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        if (editingTitle) {
                            OutlinedTextField(
                                value = templateTitle,
                                onValueChange = { templateTitle = it },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    repo.updateTemplateTitle(templateId, templateTitle.trim())
                                    editingTitle = false
                                },
                                enabled = templateTitle.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Title")
                            }

                        } else {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(templateTitle)
                                Text(
                                    "Edit",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { editingTitle = true }
                                )
                            }
                        }
                    }
                }
            }

            // ---------- Info ----------
            item {
                Text(
                    "These fields will appear to students when they apply for jobs using this template.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // ---------- Add / Edit Field ----------
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        if (viewModel.editingField != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Editing: ${viewModel.editingField!!.label}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )


                                    Text(
                                        "Cancel",
                                        color = Color.Red,
                                        modifier = Modifier.clickable {
                                            viewModel.clearEditing()
                                            label = ""
                                            optionsText = ""
                                            required = false
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                        }

                        Text("Add New Field")
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Field Label") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Field Type") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                fieldTypes.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it) },
                                        onClick = {
                                            selectedType = it
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = required, onCheckedChange = { required = it })
                            Text("Required")
                        }

                        if (selectedType == "dropdown") {
                            OutlinedTextField(
                                value = optionsText,
                                onValueChange = { optionsText = it },
                                label = { Text("Options (comma separated)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val id = viewModel.editingField?.fieldId
                                    ?: UUID.randomUUID().toString()

                                val field = FormField(
                                    fieldId = id,
                                    label = label.trim(),
                                    type = selectedType,
                                    required = required,
                                    order = viewModel.fields.indexOfFirst { it.fieldId == id }
                                        .takeIf { it != -1 }?.plus(1)
                                        ?: (viewModel.fields.size + 1),

                                    options = if (selectedType == "dropdown")
                                        optionsText.split(",").map { it.trim() }
                                    else emptyList(),
                                )

                                viewModel.addOrUpdateField(field)
                                label = ""
                                optionsText = ""
                                required = false
                                viewModel.clearEditing()
                            },
                            enabled = label.trim().isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (viewModel.editingField == null) "Add Field" else "Update Field")
                        }
                    }
                }
            }

            // ---------- Live Preview ----------
            item {
                Text("Live Preview", fontWeight = FontWeight.Bold)
            }

            items(viewModel.fields, key = { it.fieldId }) { field ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(Modifier.weight(1f)) {
                            Text(
                                buildAnnotatedString {
                                    append("${viewModel.fields.indexOf(field) + 1}. ${field.label}")
                                    if (field.required) {
                                        append(" ")
                                        withStyle(SpanStyle(color = Color.Red)) { append("*") }
                                    }
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(field.type, color = Color.Gray)
                        }

                        Row {
                            IconButton(
                                onClick = { viewModel.moveFieldUp(field) },
                                enabled = viewModel.fields.indexOf(field) != 0
                            ) { Text("⬆") }

                            IconButton(
                                onClick = { viewModel.moveFieldDown(field) },
                                enabled = viewModel.fields.indexOf(field) != viewModel.fields.lastIndex
                            ) { Text("⬇") }


                            Text(
                                "Edit",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    label = field.label
                                    selectedType = field.type
                                    required = field.required
                                    optionsText = field.options.joinToString(", ")
                                    viewModel.startEditing(field)

                                    scope.launch {
                                        listState.animateScrollToItem(2) //  index of Add/Edit Field card
                                    }
                                }
                            )


                            Spacer(Modifier.width(8.dp))

                            Text(
                                "Delete",
                                color = Color.Red,
                                modifier = Modifier.clickable {
                                    viewModel.deleteField(field)
                                    repo.deleteFieldFromTemplate(templateId, field.fieldId)
                                }
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    "⚠️ Changes to this template will apply only to future jobs. " +
                            "Existing job forms will not be affected.",
                    color = Color(0xFFE65100),
                    style = MaterialTheme.typography.bodySmall
                )
            }


            // ---------- Save ----------
            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.saveAll(templateId)
                        navController.navigateUp()

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Template")
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}


