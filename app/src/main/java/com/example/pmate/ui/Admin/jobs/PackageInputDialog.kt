package com.example.pmate.ui.Admin.jobs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun PackageInputDialog(
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var lpa by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Offered Package (LPA)") },
        text = {
            OutlinedTextField(
                value = lpa,
                onValueChange = { lpa = it },
                label = { Text("Package in LPA") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(lpa.toDoubleOrNull() ?: 0.0)
                },
                enabled = lpa.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
