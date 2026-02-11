package com.example.pmate.ui.Admin.DynamicFormEngine

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pmate.Auth.LocalSessionManager
import com.example.pmate.CommonReusableUIComponents.PmateTopBar
import com.example.pmate.Firestore.DataModels.FormField
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.example.pmate.Firestore.FirestoreRepository.FormRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun JobFormScreen(
    navController: NavController,
    jobId: String
) {

    val session = LocalSessionManager.current
    val repo = remember { FormRepository(session.currentCollegeId) }
    val jobRepo = remember { FirestoreRepository(session) }

    var fields by remember { mutableStateOf<List<FormField>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val job = jobRepo.getJobById(jobId)
        val jobFormId = job?.jobFormId ?: return@LaunchedEffect

        repo.getJobFormFields(jobFormId) {
            fields = it.sortedBy { f -> f.order }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            PmateTopBar(
                title = "Application Form",
                navController = navController
            )
        }
    ) { padding ->

        if (loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        DynamicFormRenderer(
            navController = navController,
            fields = fields,
            jobId = jobId,
            modifier = Modifier.padding(padding)
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicFormRenderer(
    navController: NavController,
    fields: List<FormField>,
    jobId: String,
    modifier: Modifier = Modifier
) {

    val session = LocalSessionManager.current
    val repo = remember { FormRepository(session.currentCollegeId) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val answers = remember { mutableStateMapOf<String, String>() }
    val allRequiredFilled = fields.all { field ->
        !field.required || !answers[field.fieldId].isNullOrBlank()
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        fields.forEach { field ->

            when (field.type) {

                "text" -> FormTextField(field, answers, "Enter text here")

                "number" -> FormTextField(
                    field,
                    answers,
                    "Only numbers allowed",
                    keyboard = KeyboardType.Number
                )

                "email" -> FormTextField(
                    field,
                    answers,
                    "Enter email address",
                    keyboard = KeyboardType.Email
                )

                "phone" -> FormTextField(
                    field,
                    answers,
                    "Enter phone number",
                    keyboard = KeyboardType.Phone
                )

                "url" -> FormTextField(
                    field,
                    answers,
                    "Paste URL here",
                    keyboard = KeyboardType.Uri
                )

                "textarea" -> FormTextField(
                    field,
                    answers,
                    "Write your answer",
                    singleLine = false
                )

                "date" -> FormTextField(
                    field,
                    answers,
                    "DD/MM/YYYY"
                )

                "dropdown" -> {
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = answers[field.fieldId] ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text(buildAnnotatedString {
                                    append(field.label)
                                    if (field.required) {
                                        append(" ")
                                        withStyle(SpanStyle(color = Color.Red)) { append("*") }
                                    }
                                })
                            },
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
                            field.options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        answers[field.fieldId] = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                "radio" -> {
                    Column {
                        field.options.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    answers[field.fieldId] = option
                                }
                            ) {
                                RadioButton(
                                    selected = answers[field.fieldId] == option,
                                    onClick = { answers[field.fieldId] = option }
                                )
                                Text(option)
                            }
                        }
                    }
                }

                "checkbox" -> {
                    Column {
                        field.options.forEach { option ->
                            val current =
                                answers[field.fieldId]?.split(",")?.toMutableSet() ?: mutableSetOf()

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = current.contains(option),
                                    onCheckedChange = {
                                        if (it) current.add(option) else current.remove(option)
                                        answers[field.fieldId] = current.joinToString(",")
                                    }
                                )
                                Text(option)
                            }
                        }
                    }
                }
            }

        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {

                    val studentId = FirebaseAuth.getInstance().currentUser!!.uid

                    repo.submitApplication(jobId, studentId, answers)

                    val jobRepo = FirestoreRepository(session)
                    jobRepo.applyForJob(jobId, studentId)

                    Toast.makeText(
                        context,
                        "Application Submitted Successfully",
                        Toast.LENGTH_LONG
                    ).show()

                    navController.popBackStack()
                }
            },
            enabled = allRequiredFilled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Submit Application")
        }
        if (!allRequiredFilled) {
            Text(
                "Please fill all required fields (*)",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

    }
}

@Composable
fun FormTextField(
    field: FormField,
    answers: MutableMap<String, String>,
    hint: String,
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = answers[field.fieldId] ?: "",
        onValueChange = { answers[field.fieldId] = it },
        label = {
            Text(buildAnnotatedString {
                append(field.label)
                if (field.required) {
                    append(" ")
                    withStyle(SpanStyle(color = Color.Red)) { append("*") }
                }
            })
        },
        placeholder = { Text(hint) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth()
    )
}
